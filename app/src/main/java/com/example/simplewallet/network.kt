package com.example.simplewallet

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zk_controlled_mobile_sdk.EthereumClient

data class TransactionLog(
    @SerializedName("to") val to: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("time") val time: Long
)

fun getTransactionHistory(
    contractAddress: String,
    offset: Int,
    limit: Int,
    onResult: (List<TransactionLog>) -> Unit,
) {
    val client = EthereumClient().newEthereumClient(BuildConfig.RPC_STRING)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val transactionHistoryJson =
                client.getTransactionHistory(contractAddress, offset.toString(), limit.toString())

            if (!transactionHistoryJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<TransactionLog>>() {}.type
                val transactionHistory: List<TransactionLog> =
                    Gson().fromJson(transactionHistoryJson, type)

                onResult(transactionHistory)
                return@launch
            }

            onResult(emptyList())
        } catch (e: Exception) {
            Log.d("GetTransactionHistory", "Failed to fetch transaction history: $e")

            onResult(emptyList())
        }
    }
}

fun getPredictedAccountAddressAndBalance(
    context: Context,
    privateKey: String,
    onResult: (String, String, String) -> Unit
) {
    val client = EthereumClient().newEthereumClient(BuildConfig.RPC_STRING)

    try {
        val accountAddress = client.getPredictedAccountAddress(privateKey, BuildConfig.EVENT_ID_STRING, BuildConfig.SMART_ACCOUNT_FACTORY_ADDRESS)
        val ethAccountAddress = client.getEthAddress(privateKey, BuildConfig.CHAIN_ID)
        val accountBalance = client.getContractBalance(accountAddress)

        onResult(accountAddress, ethAccountAddress, accountBalance)
    } catch (e: Exception) {
        showToast(context, "Failed to fetch account address.")
        Log.d("GetPredictedAccountAddress", "Failed to fetch account address: $e")
    }
}

fun getSendETHInputs(
    privateKey: String,
    receiver: String,
    amount: String,
    onResult: (String) -> Unit
) {
    val client = EthereumClient().newEthereumClient(BuildConfig.RPC_STRING)

    try {
        val accountAddress = client.getAccountDeployed(privateKey, BuildConfig.EVENT_ID_STRING, BuildConfig.SMART_ACCOUNT_FACTORY_ADDRESS)

        val inputs = client.getSendETHInputs(
            privateKey,
            BuildConfig.EVENT_ID_STRING,
            receiver,
            amount,
            accountAddress,
            BuildConfig.SMART_ACCOUNT_FACTORY_ADDRESS,
            BuildConfig.ENTRY_POINT_ADDRESS,
            BuildConfig.PAYMASTER_ADDRESS
        )

        onResult(inputs)
    } catch (e: Exception) {
        Log.d("GetSendETHInputs", "Failed to fetch inputs: $e")
    }
}

fun sendETH(
    privateKey: String,
    receiver: String,
    amount: String,
    proof: String,
    onResult: (String) -> Unit,
    onFailure: () -> Unit
) {
    val client = EthereumClient().newEthereumClient(BuildConfig.RPC_STRING)

    try {
        val accountAddress = client.getAccountDeployed(privateKey, BuildConfig.EVENT_ID_STRING, BuildConfig.SMART_ACCOUNT_FACTORY_ADDRESS)

        val inputs = client.sendETH(
            privateKey,
            BuildConfig.CHAIN_ID,
            BuildConfig.EVENT_ID_STRING,
            receiver,
            amount,
            accountAddress,
            BuildConfig.SMART_ACCOUNT_FACTORY_ADDRESS,
            BuildConfig.ENTRY_POINT_ADDRESS,
            BuildConfig.PAYMASTER_ADDRESS,
            proof
        )

        onResult(inputs)
    } catch (e: Exception) {
        Log.d("sendETH", "Failed to send transaction: $e")

        onFailure()
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
