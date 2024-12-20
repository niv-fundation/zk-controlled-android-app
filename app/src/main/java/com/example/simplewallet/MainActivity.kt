package com.example.simplewallet

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.simplewallet.ui.theme.AppTheme
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    val context = LocalContext.current

    val isSystemDarkTheme = isSystemInDarkTheme()

    var isPrivateKeySaved by rememberSaveable { mutableStateOf(false) }
    var isDarkThemeEnabled by remember { mutableStateOf(isSystemDarkTheme) }

    var isBiometricsEnabled by rememberSaveable { mutableStateOf(isBiometricsEnabled(context)) }
    var isBiometricSupported by remember { mutableStateOf(false) }
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }

//    LaunchedEffect(Unit) {
//        withContext(Dispatchers.Default) {
//            val input = AuthProofInput(
//                skI = "10294389393062768729385114272199841316649911726454061560509573309549756959666",
//                eventID = "5",
//                messageHash = "13340980865108578023729260889913910942608023953420227864452550051784760977379",
//                signatureR8x = "2219620931074135295298562935610427153265311869112466120792985987361809794174",
//                signatureR8y = "6874592126905485210199822599351206798719043154484138943505165137062408985995",
//                signatureS = "1808649777735465551464980702439997373577352451918028021190311136991955101595"
//            )
//
//            Log.d("MainActivity", "input: ${input.toJson()}")
//            val kek = ZKPUseCase(context, context.assets).generateZKP(
//                "IdentityAuth.zkey",
//                R.raw.auth_dat,
//                input.toJson().toByteArray(),
//                ZKPUtil::auth
//            )
//
//            Log.d("MainActivity", "kek: ${GsonBuilder().setPrettyPrinting().create().toJson(kek)}")
//        }
//    }

    LaunchedEffect(Unit) {
        val privateKey = withContext(Dispatchers.IO) {
            getPrivateKey(context)
        }
        isPrivateKeySaved = privateKey != null

        isBiometricSupported = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS

        if (isPrivateKeySaved && isBiometricsEnabled && isBiometricSupported && !isAuthenticated) {
            authenticateWithBiometrics(
                context = context,
                onSuccess = { isAuthenticated = true },
                onFailure = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    (context as? Activity)?.finish()
                }
            )
        } else if (!isBiometricsEnabled || !isBiometricSupported) {
            isAuthenticated = true
        }
    }

    if (isAuthenticated) {
        AppTheme(darkTheme = isDarkThemeEnabled) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppHeader()
                }
            ) { innerPadding ->
                if (isPrivateKeySaved) {
                    InteractionScreen(
                        isDarkThemeEnabled = isDarkThemeEnabled,
                        onDarkThemeToggle = { isDarkThemeEnabled = it },
                        modifier = Modifier.padding(innerPadding),
                        isBiometricSupported = isBiometricSupported,
                        isBiometricsEnabled = isBiometricsEnabled,
                        onBiometricsToggle = { enabled ->
                            isBiometricsEnabled = enabled
                            saveBiometricsEnabled(context, enabled)
                        }
                    )
                } else {
                    PrivateKeyScreen(
                        onPrivateKeySave = { isSaved ->
                            isPrivateKeySaved = isSaved
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppHeader() {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(stringResource(R.string.simple_wallet_title))
        },
    )
}

@Composable
fun InteractionScreen(
    isDarkThemeEnabled: Boolean,
    onDarkThemeToggle: (Boolean) -> Unit,
    isBiometricSupported: Boolean,
    isBiometricsEnabled: Boolean,
    onBiometricsToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSelectedNavigationElement by remember { mutableStateOf(SelectedNavigationElement.Home) }

    val privateKey = getPrivateKey(LocalContext.current)
    if (privateKey == null) {
        Log.e("InteractionScreen", "Private key is null")
        return
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            WalletBottomNavigation(
                selectedNavigationElement = currentSelectedNavigationElement,
                onItemSelected = { selectedItem ->
                    currentSelectedNavigationElement = selectedItem
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSelectedNavigationElement) {
                SelectedNavigationElement.Home -> {
                    HomeScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                SelectedNavigationElement.Send -> {
                    SendScreen(modifier = Modifier.fillMaxSize())
                }

                SelectedNavigationElement.Settings -> {
                    SettingsScreen(
                        context = LocalContext.current,
                        isBiometricsEnabled = isBiometricsEnabled,
                        onBiometricsToggle = onBiometricsToggle,
                        isDarkThemeEnabled = isDarkThemeEnabled,
                        onDarkThemeToggle = onDarkThemeToggle,
                        modifier = Modifier.fillMaxSize(),
                        isBiometricsSupported = isBiometricSupported
                    )
                }
            }
        }
    }
}

@Composable
fun WalletBottomNavigation(
    selectedNavigationElement: SelectedNavigationElement,
    onItemSelected: (SelectedNavigationElement) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.bottom_navigation_send),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            selected = selectedNavigationElement == SelectedNavigationElement.Home,
            onClick = {
                onItemSelected(SelectedNavigationElement.Home)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.bottom_navigation_send),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            selected = selectedNavigationElement == SelectedNavigationElement.Send,
            onClick = {
                onItemSelected(SelectedNavigationElement.Send)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.bottom_navigation_settings),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            selected = selectedNavigationElement == SelectedNavigationElement.Settings,
            onClick = {
                onItemSelected(SelectedNavigationElement.Settings)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppHeaderPreview() {
    AppTheme {
        AppHeader()
    }
}

@Preview(showBackground = true)
@Composable
fun WalletBottomNavigationPreview() {
    AppTheme {
        WalletBottomNavigation(SelectedNavigationElement.Settings, {})
    }
}

@Preview(showBackground = true)
@Composable
fun InteractionScreenPreview() {
    AppTheme {
        InteractionScreen(
            isDarkThemeEnabled = false,
            onDarkThemeToggle = {},
            isBiometricSupported = true,
            isBiometricsEnabled = false,
            onBiometricsToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    App()
}