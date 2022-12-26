package com.net.routee.biometric

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume

class BiometricDetection(context: Context) : ContextWrapper(context) {

    // the object for status of biometric which sends to application
    private var fingerPrintDialogContinue: CancellableContinuation<BiometricFingerPrintStatus>? = null

    // executor to use for biometric prompt
    private var executor: Executor = ContextCompat.getMainExecutor(this)

    // call back for
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    notifyUser("Authentication error: $errString")
                    fingerPrintDialogContinue?.resumeIfActive(BiometricFingerPrintStatus.FAILED)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    notifyUser("Authentication success!")
                    fingerPrintDialogContinue?.resumeIfActive(BiometricFingerPrintStatus.SUCCESS)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    notifyUser("Authentication cancelled!")
                    fingerPrintDialogContinue?.resumeIfActive(BiometricFingerPrintStatus.CANCELED)

                }
            }

    /**
     * Opening biometric dialog box.
     *
     * @param activity It is for showing pop-up of authentication.
     * @param title Field on the prompt.
     * @param subTitle Field on the prompt.
     * @param description Field on the prompt.
     * @return result of biometric status.
     */
    suspend fun openBioMetricPrompt(
        activity: FragmentActivity,
        title: String? = null,
        subTitle: String? = null,
        description: String? = null,
        authenticationCallback: BiometricPrompt.AuthenticationCallback? = null
    ): BiometricFingerPrintStatus = suspendCancellableCoroutine {
        fingerPrintDialogContinue = it
        val biometricPrompt = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title ?: "Title of Authentication")
            .setSubtitle(subTitle ?: "Authentication was required")
            .setDescription(description ?: "Need authorization to use this application.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK
                    or BiometricManager.Authenticators.DEVICE_CREDENTIAL)



        BiometricPrompt(
            activity,
            executor,
            authenticationCallback ?: this.authenticationCallback
        ).authenticate(biometricPrompt.build())
    }

    /**
     * It will check the biometric support in device and it will
     * open a dialog box if the device won't have permissions.
     *
     * @return status of biometric result.
     */
    fun checkBiometricSupport(): String {
        val manager: BiometricManager = BiometricManager.from(this)
        val status: String
        when (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> status = "BIOMETRIC_SUCCESS"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> status = "BIOMETRIC_ERROR_NO_HARDWARE"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> status =
                "BIOMETRIC_ERROR_HW_UNAVAILABLE"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                status = "BIOMETRIC_ERROR_NONE_ENROLLED"

                // opening a dialog box if the permission were not given
                MaterialAlertDialogBuilder(this)
                    .setTitle("Open setting")
                    .setMessage("Please enable credentials to use this application.")
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Okay") { dialog, _ ->
                        dialog.dismiss()
                        enrollUserToEnableCredential()
                    }
                    .show()
            }
            else -> status = "BIOMETRIC_UNKNOWN"
        }
        return status
    }

    /**
     * Opening setting based on the device version.
     *
     */
    private fun enrollUserToEnableCredential() {
        val intent: Intent = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Intent(Settings.ACTION_BIOMETRIC_ENROLL).putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> Intent(
                Settings.ACTION_FINGERPRINT_ENROLL
            )
            else -> Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )
        this.startActivity(intent)
    }

    /**
     * This function is just to show the users to status messages for biometric.
     *
     * @param message It will show to user application in toast.
     */
    fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun <T> CancellableContinuation<T>.resumeIfActive(value: T) {
        if(isActive) {
            resume(value)
        }
    }
}