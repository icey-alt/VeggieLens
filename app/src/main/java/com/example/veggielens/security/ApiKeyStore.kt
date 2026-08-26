package com.example.veggielens.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class ApiKeyStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    init {
        migrateLegacyKeyIfNeeded()
    }

    fun read(): String {
        val encryptedValue = preferences.getString(ENCRYPTED_KEY, null) ?: return ""
        val initializationVector = preferences.getString(IV_KEY, null) ?: return ""
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(128, Base64.decode(initializationVector, Base64.NO_WRAP))
            )
            String(cipher.doFinal(Base64.decode(encryptedValue, Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrDefault("")
    }

    fun write(apiKey: String) {
        if (apiKey.isBlank()) {
            preferences.edit {
                remove(ENCRYPTED_KEY)
                remove(IV_KEY)
                remove(LEGACY_KEY)
            }
            return
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encryptedValue = cipher.doFinal(apiKey.trim().toByteArray(Charsets.UTF_8))
        preferences.edit {
            putString(ENCRYPTED_KEY, Base64.encodeToString(encryptedValue, Base64.NO_WRAP))
            putString(IV_KEY, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            remove(LEGACY_KEY)
        }
    }

    private fun migrateLegacyKeyIfNeeded() {
        if (preferences.contains(ENCRYPTED_KEY)) return
        val legacyKey = preferences.getString(LEGACY_KEY, null) ?: return
        write(legacyKey)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }

    companion object {
        const val PREFERENCES_NAME = "veggielens_prefs"
        private const val LEGACY_KEY = "deepseek_api_key"
        private const val ENCRYPTED_KEY = "deepseek_api_key_encrypted"
        private const val IV_KEY = "deepseek_api_key_iv"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "veggielens_deepseek_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}