package com.example.simplewallet

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

fun savePrivateKey(context: Context, privateKey: String): String? {
    val validationError = validatePrivateKey(privateKey)
    if (validationError != null) {
        return validationError
    }

    return try {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        with(sharedPreferences.edit()) {
            putString("private_key", privateKey)
            apply()
        }

        null
    } catch (e: Exception) {
        "Failed to save the private key securely: ${e.localizedMessage ?: "Unknown error"}"
    }
}

fun getPrivateKey(context: Context): String? {
    return try {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPreferences.getString("private_key", null)
    } catch (e: Exception) {
        null
    }
}

private const val PREFERENCES_FILE_KEY = "com.example.simplewallet"
private const val BIOMETRICS_ENABLED_KEY = "biometrics_enabled"

fun saveBiometricsEnabled(context: Context, isEnabled: Boolean) {
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putBoolean(BIOMETRICS_ENABLED_KEY, isEnabled)
        apply()
    }
}

fun isBiometricsEnabled(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(BIOMETRICS_ENABLED_KEY, false)
}
