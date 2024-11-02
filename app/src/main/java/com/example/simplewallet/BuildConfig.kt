package com.example.simplewallet

object BuildConfig {
    val ETH_ACCOUNT_ADDRESS_KEY: String
        get() = "eth_account_address"

    val ACCOUNT_BALANCE_KEY: String
        get() = "account_balance"

    val ACCOUNT_ADDRESS_KEY: String
        get() = "account_address"

    val APP_SECURE_PREFERENCES: String
        get() = "secure_prefs"

    val PRIVATE_KEY_FIELD_NAME: String
        get() = "private_key"

    val APP_PREFERENCES: String
        get() = "app_preferences"

    val BIOMETRICS_ENABLED_KEY: String
        get() = "biometrics_enabled"

    val APPLICATION_ID: String
        get() = "com.example.simplewallet"

    val RPC_STRING: String
        get() = "https://eth-sepolia.g.alchemy.com/v2/-Jm280LvZnniBfiaxZtQa_wL1b_okXCZ"

    val CHAIN_ID: String
        get() = "11155111"

    val SMART_ACCOUNT_FACTORY_ADDRESS: String
        get() = "0xeF0aB9F24f826657E11B6b4E3a682ee46254A22C"

    val ENTRY_POINT_ADDRESS: String
        get() = "0x0000000071727De22E5E9d8BAf0edAc6f37da032"

    val PAYMASTER_ADDRESS: String
        get() = "0xe8CCF8dd49e297C357FDc1f84f9A6E2FED83C426"

    val EVENT_ID_STRING: String
        get() = "5"
}
