package com.example.simplewallet

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun handleSendEthClick(
    context: Context,
    recipientAddress: String,
    amount: String,
    onPhaseChange: (String) -> Unit,
    onTransactionComplete: (Boolean) -> Unit
) {
    val privateKey = getPrivateKey(context)
    if (privateKey == null) {
        Log.e("InteractionScreen", "Private key is null")
        onPhaseChange("Error: Private key not found")
        return
    }

    val uoStr = withContext(Dispatchers.Default) {
        getUO(privateKey, recipientAddress, amount)
    }

    onPhaseChange("Generating zero-knowledge proof")
    val inputJson = withContext(Dispatchers.Default) {
        getSendETHInputsSuspend(
            privateKey,
            uoStr
        )
    }

    val inputs = AuthProofInput.fromJson(inputJson)

    val proof = withContext(Dispatchers.Default) {
        ZKPUseCase(context, context.assets).generateZKP(
            "IdentityAuth.zkey",
            R.raw.auth_dat,
            inputs.toJson().toByteArray(),
            ZKPUtil::auth
        )
    }

    onPhaseChange("Sending Ethereum transaction")

    var isTransactionComplete = false
    val onTransactionStateChange = { state: Boolean ->
        isTransactionComplete = state
    }

    val uoHash = withContext(Dispatchers.Default) {
        sendETHSuspend(
            uoStr,
            proof.proof.toJson(),
            onTransactionStateChange
        )
    }

    if (isTransactionComplete) {
        onPhaseChange("Waiting for confirmation")
        var isUOConfirmed = false
        var maxRetries = 3

        while (!isUOConfirmed && maxRetries > 0) {
            delay(2000)
            isUOConfirmed = withContext(Dispatchers.Default) { isUOConfirmed(uoHash) }
            maxRetries--
        }
    }

    onTransactionComplete(isTransactionComplete)
}

suspend fun getUO(
    privateKey: String,
    receiver: String,
    amount: String
): String =
    suspendCoroutine { cont ->
        getUO(privateKey, receiver, amount, onResult = { result ->
            cont.resume(result)
        })
    }

// Wrapper suspend functions for getSendETHInputs and sendETH
suspend fun getSendETHInputsSuspend(
    privateKey: String,
    uoStr: String
): String =
    suspendCoroutine { cont ->
        getSendETHInputs(privateKey, uoStr, onResult = { result ->
            cont.resume(result)
        })
    }

suspend fun sendETHSuspend(
    uoStr: String,
    proofJson: String,
    onTransactionStateChange: (Boolean) -> Unit
): String =
    suspendCoroutine { cont ->
        sendETH(uoStr, proofJson, onResult = { txHash ->
            onTransactionStateChange(true)
            cont.resume(txHash)
        }, onFailure = {
            onTransactionStateChange(false)
            cont.resume("")
        })
    }

suspend fun isUOConfirmed(uoHash: String): Boolean =
    suspendCoroutine { cont ->
        isUOConfirmed(uoHash, onResult = { result ->
            cont.resume(result)
        })
    }

@Composable
fun SendScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var recipientAddress by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    var addressErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var amountErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var currentPhase by rememberSaveable { mutableStateOf("Starting transaction") }
    var isTransactionComplete by rememberSaveable { mutableStateOf(false) }
    var isTransactionSuccessful by rememberSaveable { mutableStateOf(false) }

    val onDismRequest = {
        showDialog = false
    }

    val coroutineScope = rememberCoroutineScope()

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
            onClick = {
                coroutineScope.launch {
                    showDialog = true
                    isTransactionComplete = false
                    currentPhase = "Initializing transaction"


                    handleSendEthClick(
                        context,
                        recipientAddress,
                        formatEthAmount(amount),
                        onPhaseChange = { phase ->
                            currentPhase = phase
                        },
                        onTransactionComplete = { isComplete ->
                            isTransactionComplete = true
                            isTransactionSuccessful = isComplete

                            currentPhase = if (isTransactionSuccessful) {
                                "Transaction completed"
                            } else {
                                "Transaction failed"
                            }
                        }
                    )

                    delay(6000)
                    showDialog = false
                }
            },
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

    if (showDialog) {
        DialogModule(isTransactionComplete, isTransactionSuccessful, currentPhase, onDismRequest)
    }
}

fun formatEthAmount(amount: String): String {
    return (amount.toDouble() * 1e18).toLong().toString()
}

@Composable
fun DialogModule(
    isTransactionComplete: Boolean,
    isTransactionSuccessful: Boolean,
    currentPhase: String,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = {
        onDismissRequest()
    }) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.wrapContentSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .size(220.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isTransactionComplete) {
                    if (isTransactionSuccessful) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Transaction successful",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(72.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Transaction failed",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                } else {
                    CircularProgressIndicator(Modifier.size(72.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentPhase,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SendScreenPreview() {
    SendScreen()
}

@Preview(showBackground = true)
@Composable
fun DialogModulePreview() {
    DialogModule(
        isTransactionComplete = false,
        isTransactionSuccessful = true,
        currentPhase = "Initializing transaction",
        onDismissRequest = {})
}

@Preview(showBackground = true)
@Composable
fun DialogModulePreviewBig() {
    DialogModule(
        isTransactionComplete = false,
        isTransactionSuccessful = true,
        currentPhase = "Generating zero-knowledge proof",
        onDismissRequest = {})
}

@Preview(showBackground = true)
@Composable
fun DialogModulePreviewComplete() {
    DialogModule(
        isTransactionComplete = true,
        isTransactionSuccessful = true,
        currentPhase = "Transaction completed",
        onDismissRequest = {})
}

@Preview(showBackground = true)
@Composable
fun DialogModulePreviewFailed() {
    DialogModule(
        isTransactionComplete = true,
        isTransactionSuccessful = false,
        currentPhase = "Transaction failed",
        onDismissRequest = {})
}