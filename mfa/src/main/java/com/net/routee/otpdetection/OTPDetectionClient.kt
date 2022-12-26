package com.net.routee.otpdetection

import android.content.*
import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.net.routee.preference.SharedPreference

class OTPDetectionClient(context: Context) : ContextWrapper(context) {

    // for notifying in otp screen
    val otpMessage = MutableLiveData("")

    /**
     * This is the method to start the sms handling service from our application
     *
     */
    fun startSMSRetriever() {
        val receiver = MySMSBroadcastReceiver()
        this.registerReceiver(receiver, IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION))
        startSMSRetrieverClient()
    }

    /**
     * Starting sms retriever
     *
     */
    private fun startSMSRetrieverClient() {
        val client = SmsRetriever.getClient(this)
        val task: Task<Void> = client.startSmsRetriever()
        task.addOnSuccessListener {
            Log.i("sms", "task started")
        }
        task.addOnFailureListener {
            Log.i("sms", "task Failed")
        }
    }


    inner class MySMSBroadcastReceiver : BroadcastReceiver() {


        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status

                // Extract one-time code from the message and complete verification
                // by sending the code back to your server.
                // Waiting for SMS timed out (5 minutes)
                // Handle the error ...
                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get SMS message contents
                        val sms = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                        Log.i("sms", sms)
                        otpMessage.postValue(retrieveOTPFromString(sms))
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        Log.i("sms", "timed out no message received")
                    }
                }
            }
        }
    }

    /**
     * Return: The 4 digit OTP from the message we got.
     */
    fun retrieveOTPFromString(sms: String): String {
        val preference = SharedPreference(this)
        val wordsList = sms.split(" ")
        if (wordsList.contains(preference.getPrefixForOTP()))
            wordsList.forEach {
                if (it.startsWith(preference.getPrefixForOTP())) {
                    it.substringAfter("-").let { otp ->
                        if (otp.length == preference.getNoOfDigitsForOTP() && otp.isDigitsOnly())
                            return otp
                    }
                }
            }
        else
            wordsList.forEach {otp ->
                        if (otp.length == preference.getNoOfDigitsForOTP() && otp.isDigitsOnly())
                            return otp
                    }

        // return all zeros if there was no 4 digit OTP present in received message.
        return "0000"
    }

    fun retrieveWithHash(sms: String?): String {
        val wordsList = sms?.split(" ")
        wordsList?.forEach {
            if (it.startsWith("#"))
                return it.removePrefix("#")
        }
        return "0000"
    }

}