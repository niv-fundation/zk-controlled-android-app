package com.example.simplewallet

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.GsonBuilder

fun handleSendEthClick(
    context: Context,
    recipientAddress: String,
    amount: String
) {
    val privateKey = getPrivateKey(context)
    if (privateKey == null) {
        Log.e("InteractionScreen", "Private key is null")
        return
    }

    getSendETHInputs(privateKey, recipientAddress, amount, onResult = {
        val inputs = AuthProofInput.fromJson(it)

        val proof = ZKPUseCase(context, context.assets).generateZKP(
            "IdentityAuth.zkey",
            R.raw.auth_dat,
            inputs.toJson().toByteArray(),
            ZKPUtil::auth
        )

        sendETH(privateKey, recipientAddress, amount, proof.proof.toJson(), onResult = { txHash ->
            Log.d("InteractionScreen", "ETH sent successfully: $txHash")
        })
    })
}

@Composable
fun SendScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var recipientAddress by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    var addressErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var amountErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = recipientAddress,
            onValueChange = {
                recipientAddress = it
                addressErrorMessage = validateRecipientAddress(it)
            },
            label = { Text(stringResource(R.string.recipient_address_hint)) },
            placeholder = { Text(stringResource(R.string.recipient_address_placeholder)) },
            isError = addressErrorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (addressErrorMessage != null) {
            Text(
                text = addressErrorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                amountErrorMessage = validateAmount(it)
            },
            label = { Text(stringResource(R.string.amount_hint)) },
            placeholder = { Text(stringResource(R.string.amount_placeholder)) },
            modifier = Modifier.fillMaxWidth()
        )

        if (amountErrorMessage != null) {
            Text(
                text = amountErrorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { handleSendEthClick(context, recipientAddress, amount) },
            enabled = addressErrorMessage == null && amountErrorMessage == null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(stringResource(R.string.send_eth_btn_title))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SendScreenPreview() {
    SendScreen()
}