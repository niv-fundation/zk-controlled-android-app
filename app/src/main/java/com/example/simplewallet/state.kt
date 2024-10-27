package com.example.simplewallet

import android.content.Context
import android.content.Intent
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

fun savePrivateKey(context: Context, privateKey: String): String? {
    val validationError = validatePrivateKey(privateKey)
    if (validationError != null) {
        return validationError
    }

    return try {
        setPrivateKey(context, privateKey)
    } catch (e: Exception) {
        "Failed to save the private key securely: ${e.localizedMessage ?: "Unknown error"}"
    }
}

private fun setPrivateKey(context: Context, privateKey: String): String? {
    val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        BuildConfig.APP_SECURE_PREFERENCES,
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    with(sharedPreferences.edit()) {
        putString(BuildConfig.PRIVATE_KEY_FIELD_NAME, privateKey)
        apply()
    }

    return null
}

fun getPrivateKey(context: Context): String? {
    return try {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            BuildConfig.APP_SECURE_PREFERENCES,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val key = sharedPreferences.getString(BuildConfig.PRIVATE_KEY_FIELD_NAME, null)

        if (key.isNullOrEmpty()) {
            return null
        }

        key
    } catch (e: Exception) {
        null
    }
}

fun saveBiometricsEnabled(context: Context, isEnabled: Boolean) {
    val sharedPreferences =
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putBoolean(BuildConfig.BIOMETRICS_ENABLED_KEY, isEnabled)
        apply()
    }
}

fun isBiometricsEnabled(context: Context): Boolean {
    val sharedPreferences =
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(BuildConfig.BIOMETRICS_ENABLED_KEY, false)
}

fun logOutAndRestartApp(context: Context) {
    clearUserData(context)

    val restartIntent = Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    context.startActivity(restartIntent)
}

fun clearUserData(context: Context) {
    val sharedPreferences =
        context.getSharedPreferences(BuildConfig.APP_PREFERENCES, Context.MODE_PRIVATE)
    sharedPreferences.edit().clear().apply()

    setPrivateKey(context, "")

    saveBiometricsEnabled(context, false)
}
