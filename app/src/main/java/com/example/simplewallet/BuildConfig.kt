package com.example.simplewallet

object BuildConfig {
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
        get() = "https://rpc.qtestnet.org"

    val SMART_ACCOUNT_FACTORY_ADDRESS: String
        get() = "0x76C9b5c8Bc736e58F5b54BA721571c77059CAa68"

    val EVENT_ID_STRING: String
        get() = "5"
}
