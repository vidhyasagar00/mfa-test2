package com.net.routee.interfaces

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.net.routee.biometric.BiometricDetection
import com.net.routee.biometric.BiometricFingerPrintStatus
import com.net.routee.biometric.BiometricResultCallback
import com.net.routee.location.LocationClient
import com.net.routee.otpdetection.OTPValidationActivity
import com.net.routee.services.Authenticator
import com.net.routee.services.FCMServices
import com.net.routee.utils.PermissionReqHandler
import com.net.routee.videoCall.WebViewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


interface ScreenOpenHelper {

    fun onOpenScreen(cls: Class<*>?, intent: Intent)
    fun configureReceiver(context: Context) {
        val permissionReqHandler: PermissionReqHandler? =
            if (context is ComponentActivity)
                PermissionReqHandler(context)
            else
                null
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(cxt: Context, intent: Intent) {
                intent.action?.let {
                    if (it == "ScreenType") {
                        when (intent.getStringExtra("screenType")) {
                            FCMServices.AuthType.OTP.type -> {
                                onOpenScreen(OTPValidationActivity::class.java,
                                    intent.putExtra("type", FCMServices.AuthType.OTP.type))
                            }
                            FCMServices.AuthType.PUSH_OTP.type -> {
                                onOpenScreen(OTPValidationActivity::class.java, intent)
                            }
                            FCMServices.AuthType.VIDEO_CONFERENCE.type -> {
                                onOpenScreen(WebViewActivity::class.java, intent)
                            }
                            FCMServices.AuthType.BIOMETRIC.type -> {
                                openBiometricPrompt(object : BiometricResultCallback {
                                    override fun permissionGranted(biometricFingerPrintStatus: BiometricFingerPrintStatus) {
                                        intent.putExtra("authTypes",
                                            intent.getStringExtra("authTypes")
                                                ?.replace(FCMServices.AuthType.BIOMETRIC.type, ""))
                                        when (biometricFingerPrintStatus) {
                                            BiometricFingerPrintStatus.SUCCESS ->
                                                Authenticator.publishResult(Authenticator.success,
                                                    context, intent)
                                            BiometricFingerPrintStatus.FAILED ->
                                                Authenticator.publishResult(Authenticator.failed,
                                                    context, intent)
                                            else -> {}
                                        }

                                    }
                                }, context, intent)
                            }
                            FCMServices.AuthType.LOCATION.type -> {
                                LocationClient(context, null, intent).getLatestLocation()
                            }

                        }
                    } else if (it == "PermissionSingle") {
                        permissionReqHandler?.request(intent.getStringArrayExtra("req"))
                    }
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction("ScreenType")
        filter.addAction("PermissionSingle")
        context.registerReceiver(receiver, filter)
    }


    fun unregisterReceiver(context: Context) {
//        context.unregisterReceiver(receiver)
    }

    private fun openBiometricPrompt(
        biometricResultCallback: BiometricResultCallback,
        context: Context, intent: Intent,
    ) {
        val biometricDetector = BiometricDetection(context)

        //Check for the Biometric support
        if (biometricDetector.checkBiometricSupport() == "BIOMETRIC_SUCCESS") {
            CoroutineScope(Dispatchers.Main).launch {


                val authenticationCallback: BiometricPrompt.AuthenticationCallback= object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            biometricResultCallback.permissionGranted(BiometricFingerPrintStatus.FAILED)
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            biometricResultCallback.permissionGranted(BiometricFingerPrintStatus.SUCCESS)
                           }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            biometricResultCallback.permissionGranted(BiometricFingerPrintStatus.CANCELED)
                             }
                    }

                //Open the Biometric Dialog
                if (context is FragmentActivity) {
                    val status = biometricDetector.openBioMetricPrompt(context,
                        intent.extras?.getString("title"),
                        intent.extras?.getString("subTitle"),
                        intent.extras?.getString("description"),
                        authenticationCallback)


                }
            }
        }


    }


}