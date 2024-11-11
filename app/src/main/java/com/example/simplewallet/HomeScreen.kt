package com.example.simplewallet

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.materii.pullrefresh.PullRefreshIndicator
import dev.materii.pullrefresh.pullRefresh
import dev.materii.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun AccountRow(accountTitle: String, accountBalance: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = stringResource(R.string.account_icon_content_desc),
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = accountTitle,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Text(
            text = accountBalance,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CopyableAccountAddressText(accountAddress: String?) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Text(
        text = accountAddress ?: stringResource(R.string.loading_account_address),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                clipboardManager.setText(
                    androidx.compose.ui.text.AnnotatedString(
                        accountAddress ?: ""
                    )
                )
                Toast
                    .makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                    .show()
            },
        textAlign = TextAlign.Center
    )
}

@Composable
fun AccountBar(accountAddress: String?, accountBalance: String, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AccountRow(
                accountTitle = stringResource(R.string.account_card_title),
                accountBalance = accountBalance
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            CopyableAccountAddressText(accountAddress)
        }
    }
}

suspend fun getTransactionHistorySuspend(
    accountAddress: String,
    offset: Int,
    limit: Int
): List<TransactionLog> =
    suspendCoroutine { cont ->
        getTransactionHistory(accountAddress, offset, limit) { result ->
            cont.resume(result)
        }
    }

@Composable
fun TransactionHistory(
    accountAddress: String?,
    modifier: Modifier = Modifier
) {
    var transactions by rememberSaveable { mutableStateOf<List<TransactionLog>>(emptyList()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var hasMoreData by rememberSaveable { mutableStateOf(true) }
    var offset by rememberSaveable { mutableIntStateOf(0) }
    val limit = 10

    val coroutineScope = rememberCoroutineScope()

    fun fetchTransactionHistory() {
        if (isLoading || !hasMoreData || accountAddress == null) return

        isLoading = true

        coroutineScope.launch {
            val newTransactions = getTransactionHistorySuspend(accountAddress, offset, limit)
            if (newTransactions.isEmpty()) {
                hasMoreData = false
            } else {
                transactions = transactions + newTransactions
                offset += limit
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit, accountAddress) {
        if (accountAddress != null) {
            coroutineScope.launch {
                withContext(Dispatchers.Default) {
                    fetchTransactionHistory()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.transaction_history_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(transactions) { transaction ->
                TransactionCard(transaction)
            }

            if (isLoading || hasMoreData) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            withContext(Dispatchers.Default) {
                                fetchTransactionHistory()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: TransactionLog, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 12.dp)
        ) {
            val formattedDate = formatTimestamp(transaction.time)

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .paddingFromBaseline(top = 16.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = stringResource(R.string.transaction_icon_content_desc),
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(8.dp),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.transaction_title),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.transaction_status_completed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = transaction.amount,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val privateKey = getPrivateKey(context)
    if (privateKey == null) {
        Log.e("InteractionScreen", "Private key is null")
        return
    }

    var isRefreshing by rememberSaveable { mutableStateOf(false) }

    var accountAddress by rememberSaveable { mutableStateOf(getAccountAddress(context)) }
    var accountBalance by rememberSaveable { mutableStateOf(getAccountBalance(context)) }

    // Fetch account address and balance function
    fun fetchAccountData() {
        if (accountAddress != null && isRefreshing) return

        isRefreshing = true
        getPredictedAccountAddressAndBalance(
            context = context,
            privateKey = privateKey
        ) { resAccountAddress, resEthAccountAddress, resAccountBalance ->
            accountAddress = resAccountAddress
            accountBalance = resAccountBalance
            
            saveAccountAddress(context, resAccountAddress)
            saveAccountBalance(context, resAccountBalance)

            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) { fetchAccountData() }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { fetchAccountData() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AccountBar(
                accountAddress = accountAddress,
                accountBalance = accountBalance,
                modifier = Modifier.paddingFromBaseline(top = 4.dp, bottom = 4.dp)
            )

            TransactionHistory(
                accountAddress = accountAddress,
                modifier = Modifier.weight(1f)
            )
        }

        PullRefreshIndicator(
            state = pullRefreshState,
            modifier = modifier.align(Alignment.TopCenter),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountRowPreview() {
    AccountRow(
        accountTitle = "Ethereum",
        accountBalance = "0.5 ETH",
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun AccountBarPreview() {
    AccountBar(
        accountAddress = "0xf41ceE234219D6cc3d90A6996dC3276aD378cfCF",
        accountBalance = "0.5 ETH"
    )
}

@Preview(showBackground = true)
@Composable
fun TransactionCardPreview() {
    TransactionCard(
        transaction = TransactionLog(
            to = "0xf41ceE234219D6cc3d90A6996dC3276aD378cfCF",
            amount = "0.5",
            time = System.currentTimeMillis()
        ),
        Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true, heightDp = 300)
@Composable
fun TransactionHistoryPreview() {
    TransactionHistory(accountAddress = "0xf41ceE234219D6cc3d90A6996dC3276aD378cfCF")
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}