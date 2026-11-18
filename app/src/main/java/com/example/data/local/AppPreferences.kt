package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "madrasa_settings")

data class AppSettingsData(
    val orgId: String = "kmnqhm_1995",
    val paymentSandboxEnabled: Boolean = true,
    val offlineFirstAttendance: Boolean = false,
    val smsGatewayEnabled: Boolean = false,
    val smsProviderName: String = "স্থানীয় SMS কিউ অ্যাডাপ্টার (প্রস্তুত)"
)

class AppPreferences(private val context: Context) {
    private object Keys {
        val ORG_ID = stringPreferencesKey("org_id")
        val PAYMENT_SANDBOX = booleanPreferencesKey("payment_sandbox")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val SMS_ENABLED = booleanPreferencesKey("sms_enabled")
        val SMS_PROVIDER = stringPreferencesKey("sms_provider")
    }

    val settingsFlow: Flow<AppSettingsData> = context.dataStore.data.map { prefs ->
        AppSettingsData(
            orgId = prefs[Keys.ORG_ID] ?: "kmnqhm_1995",
            paymentSandboxEnabled = prefs[Keys.PAYMENT_SANDBOX] ?: true,
            offlineFirstAttendance = prefs[Keys.OFFLINE_MODE] ?: false,
            smsGatewayEnabled = prefs[Keys.SMS_ENABLED] ?: false,
            smsProviderName = prefs[Keys.SMS_PROVIDER] ?: "স্থানীয় SMS কিউ অ্যাডাপ্টার (প্রস্তুত)"
        )
    }

    suspend fun updateSandboxMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PAYMENT_SANDBOX] = enabled }
    }

    suspend fun updateOfflineAttendanceMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.OFFLINE_MODE] = enabled }
    }

    suspend fun updateSmsSettings(enabled: Boolean, providerName: String) {
        context.dataStore.edit {
            it[Keys.SMS_ENABLED] = enabled
            it[Keys.SMS_PROVIDER] = providerName
        }
    }
}
