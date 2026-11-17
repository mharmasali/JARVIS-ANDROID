package com.harmas.jarvis.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecretVault(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(
            "jarvis_secure_storage",
            Context.MODE_PRIVATE
        )

    private val alias = "JARVIS_MASTER_KEY"
    private val transformation = "AES/GCM/NoPadding"

    private fun getKey(): SecretKey {

        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val existing = keyStore.getKey(alias, null)

        if (existing is SecretKey) {
            return existing
        }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setRandomizedEncryptionRequired(true)
                .build()
        )

        return generator.generateKey()
    }

    fun save(key: String, value: String) {

        require(key.isNotBlank())

        val cipher = Cipher.getInstance(transformation)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getKey()
        )

        val encrypted = cipher.doFinal(
            value.toByteArray(Charsets.UTF_8)
        )

        val iv = cipher.iv

        val encoded = Base64.encodeToString(
            encrypted,
            Base64.NO_WRAP
        )

        val encodedIv = Base64.encodeToString(
            iv,
            Base64.NO_WRAP
        )

        check(
            preferences.edit()
                .putString("${key}_data", encoded)
                .putString("${key}_iv", encodedIv)
                .commit()
        ) {
            "Could not save encrypted secret"
        }
    }

    fun read(key: String): String? {

        val encrypted = preferences.getString(
            "${key}_data",
            null
        ) ?: return null

        val iv = preferences.getString(
            "${key}_iv",
            null
        ) ?: return null

        val cipher = Cipher.getInstance(transformation)

        cipher.init(
            Cipher.DECRYPT_MODE,
            getKey(),
            GCMParameterSpec(
                128,
                Base64.decode(iv, Base64.NO_WRAP)
            )
        )

        val decrypted = cipher.doFinal(
            Base64.decode(encrypted, Base64.NO_WRAP)
        )

        return String(decrypted, Charsets.UTF_8)
    }

    fun delete(key: String) {

        preferences.edit()
            .remove("${key}_data")
            .remove("${key}_iv")
            .apply()
    }
}
