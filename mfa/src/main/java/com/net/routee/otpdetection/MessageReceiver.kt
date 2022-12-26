package com.net.routee.otpdetection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.net.routee.utils.Constants


class MessageReceiver : BroadcastReceiver() {
    /**
     * It was a default function where it has context and intent default params
     *
     * @param context Default param
     * @param intent Default param
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        val data = intent?.extras
        data?.let {
            val pdus = data["pdus"] as Array<*>?
            for (i in pdus!!.indices) {
                val smsMessage: SmsMessage? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val format: String? = it.getString("format")
                    SmsMessage.createFromPdu(pdus[i] as ByteArray?, format)
                } else {
                    SmsMessage.createFromPdu(pdus[i] as ByteArray?)
                }

                val message = smsMessage?.messageBody ?: ""
                Log.v("SMS--", message)
                // adding a broad cast receiver to send token to the application.
                val msgIntent = Intent(Constants.ACTION_FOR_SMS)
                msgIntent.putExtra("SMS", message)
                if (context != null) {
                    LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(msgIntent)
                }
            }
        }
    }
}