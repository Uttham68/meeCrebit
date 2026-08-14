package com.example.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

sealed class BiometricStatus {
    object Available : BiometricStatus()
    data class NotEnrolled(val message: String = "Biometric credentials (fingerprint/face) not enrolled on this device.") : BiometricStatus()
    data class HardwareUnavailable(val message: String = "Biometric sensor is currently busy or unavailable.") : BiometricStatus()
    data class Unsupported(val message: String = "Biometric authentication hardware is not present on this device.") : BiometricStatus()
    data class Unknown(val message: String) : BiometricStatus()
}

class BiometricAuthManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("meecrebit_security_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_BIOMETRIC_ENABLED = "pref_biometric_enabled"
        const val PREF_AUTO_LOCK_TIMEOUT = "pref_auto_lock_timeout" // in milliseconds: 0 = immediate, 30s, 60s, 300s
        const val PREF_LAST_PAUSED_TIME = "pref_last_paused_time"

        @Volatile
        private var INSTANCE: BiometricAuthManager? = null

        fun getInstance(context: Context): BiometricAuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BiometricAuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(PREF_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, value).apply()

    var autoLockTimeoutMillis: Long
        get() = prefs.getLong(PREF_AUTO_LOCK_TIMEOUT, 0L) // 0L = immediate lock on app background
        set(value) = prefs.edit().putLong(PREF_AUTO_LOCK_TIMEOUT, value).apply()

    fun checkBiometricSupport(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NotEnrolled()
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.Unsupported()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HardwareUnavailable()
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.Unknown("Security update required for biometric sensor.")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.Unsupported()
            else -> BiometricStatus.Available
        }
    }

    fun recordAppPaused() {
        prefs.edit().putLong(PREF_LAST_PAUSED_TIME, System.currentTimeMillis()).apply()
    }

    fun shouldLockOnResume(): Boolean {
        if (!isBiometricEnabled) return false
        val lastPaused = prefs.getLong(PREF_LAST_PAUSED_TIME, 0L)
        if (lastPaused == 0L) return true
        val elapsed = System.currentTimeMillis() - lastPaused
        return elapsed >= autoLockTimeoutMillis
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock meeCrebit",
        subtitle: String = "Verify identity to access offline financial ledger",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription("Touch the fingerprint sensor, use Face ID, or enter your device PIN/Password.")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
