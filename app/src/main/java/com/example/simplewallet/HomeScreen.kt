package com.example.simplewallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
fun AccountBar(accountAddress: String, accountBalance: String, modifier: Modifier = Modifier) {
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

            Text(
                text = accountAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TransactionHistory(
    transactions: List<Transaction>,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(transactions) { transaction ->
                TransactionCard(transaction)
            }

            item {
                onLoadMore()
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction, modifier: Modifier = Modifier) {
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
            val formattedDate = formatTimestamp(transaction.date)

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
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = transaction.status,
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
    accountAddress: String,
    accountBalance: String,
    transactions: List<Transaction>,
    onLoadMoreTransaction: () -> Unit,
    modifier: Modifier = Modifier
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
            transactions = transactions,
            onLoadMore = onLoadMoreTransaction,
            modifier = Modifier.weight(1f)
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

private val transactions = listOf(
    Transaction(
        title = "Sent ETH",
        amount = "-0.1 ETH",
        status = "Completed",
        date = System.currentTimeMillis()
    ),
    Transaction(
        title = "Received ETH",
        amount = "+0.5 ETH",
        status = "Confirmed",
        date = System.currentTimeMillis() - 1000 * 60 * 60 * 24
    ),
    Transaction(
        title = "Sent ETH",
        amount = "-0.2 ETH",
        status = "Completed",
        date = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2
    ),
    Transaction(
        title = "Received ETH",
        amount = "+0.3 ETH",
        status = "Confirmed",
        date = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3
    ),
    Transaction(
        title = "Sent ETH",
        amount = "-0.1 ETH",
        status = "Completed",
        date = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 4
    ),
    Transaction(
        title = "Received ETH",
        amount = "+0.2 ETH",
        status = "Confirmed",
        date = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5
    ),
    Transaction(
        title = "Sent ETH",
        amount = "-0.1 ETH",
        status = "Completed",
        date = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 6
    ),
    Transaction(
        title = "Received ETH",
        amount = "+0.1 ETH",
        status = "Confirmed",
        date = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7
    )
)

@Preview(showBackground = true)
@Composable
fun TransactionCardPreview() {
    TransactionCard(
        transaction = Transaction(
            title = "Sent ETH",
            amount = "-0.1 ETH",
            status = "Completed",
            date = System.currentTimeMillis()
        ),
        Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true, heightDp = 300)
@Composable
fun TransactionHistoryPreview() {
    TransactionHistory(
        transactions = transactions,
        onLoadMore = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        accountAddress = "0xf41ceE234219D6cc3d90A6996dC3276aD378cfCF",
        accountBalance = "0.5 ETH",
        transactions = transactions,
        onLoadMoreTransaction = {}
    )
}