package com.net.routee.otpdetection

import android.Manifest
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.internal.LinkedTreeMap
import com.net.routee.R
import com.net.routee.preference.SharedPreference
import com.net.routee.retrofit.APISupport
import com.net.routee.services.Authenticator
import com.net.routee.services.FCMServices
import com.net.routee.utils.Constants
import kotlinx.android.synthetic.main.authentication_dialog.view.*
import kotlinx.android.synthetic.main.otp_validation_xml.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class OTPValidationActivity : AppCompatActivity() {

    private lateinit var receiverOTP: BroadcastReceiver

    lateinit var preference: SharedPreference
    var trackingId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp_validation_xml)


        preference = SharedPreference(this)
        // watcher of our text view for enabling or disabling button
        textBox.doOnTextChanged { text, _, _, _ ->
            submitBTN.isEnabled = text?.length == 4
        }

        /*
            These are the line that we are used to generate otp detection client
            so you can use this particular lines to access the otp detection client.
        */
        val otpDetectionClient = OTPDetectionClient(this)
        otpDetectionClient.startSMSRetriever()

        // observer for live data to update our text field with OTP
        otpDetectionClient.otpMessage.observe(this) {
            textBox.setText(it)
        }

        intent.putExtra("authTypes", "")
        val type = intent.getStringExtra("type")
        if (type == FCMServices.AuthType.OTP.type) {
            intent.putExtra("type", "")
            openDialogBox()
        } else {
            val message = intent.getStringExtra("message")
            otpDetectionClient.retrieveWithHash(message).let {
                otpDetectionClient.otpMessage.postValue(it)

            }
        }



        submitBTN.setOnClickListener {
            val otpValue = textBox.text.toString()
            val call = APISupport.verifyOTP(Constants.OTP_UPDATE_URL,
                preference.getAccessToken(),
                trackingId,
                otpValue)
            val progress = ProgressDialog(this)
            progress.setTitle("Loading")
            progress.show()

            call?.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    progress.dismiss()
                    if (response.isSuccessful) {
//                        MaterialAlertDialogBuilder(this@OTPValidationActivity)
//                            .setTitle("Verification Success!")
//                            .setMessage("Click Ok to go back.")
//                            .setCancelable(false)
//                            .setPositiveButton("Ok") { dialog, _ ->
                        Authenticator.publishResult(Authenticator.success,
                            this@OTPValidationActivity, intent)
                        this@OTPValidationActivity.finish()
//                                dialog.dismiss()
//                            }
//                            .show()

                    } else {

                        if (this@OTPValidationActivity.isDestroyed)
                            return
//                        MaterialAlertDialogBuilder(this@OTPValidationActivity)
//                            .setTitle("Verification Failed!")
//                            .setMessage("The OTP you're entered is wrong.")
//                            .setCancelable(false)
//                            .setPositiveButton("Close") { dialog, _ ->
                        Authenticator.publishResult(Authenticator.failed,
                            this@OTPValidationActivity, intent)
                        this@OTPValidationActivity.finish()
//                                dialog.dismiss()
//                            }
//                            .show()
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    progress.dismiss()
                    Authenticator.publishResult(Authenticator.failed,
                        this@OTPValidationActivity, intent)
                    this@OTPValidationActivity.finish()
//                    MaterialAlertDialogBuilder(this@OTPValidationActivity)
//                        .setTitle("Verification Failed!")
//                        .setMessage("The OTP you're entered is wrong.")
//                        .setPositiveButton("Close") { dialog, _ ->
//                            dialog.dismiss()
//                        }
//                        .show()
                }

            })
        }

        checkKeyHash()


        this.startPermissionRequest(object : FragmentPermissionCallback {
            override fun onGranted(isGranted: Boolean) {
                if (isGranted) {
                    // permission granted continue the normal workflow of app
                    Log.i("SMS--", "permission granted")
                } else {
                    // if permission denied then check whether never ask
                    // again is selected or not by making use of
                    // !ActivityCompat.shouldShowRequestPermissionRationale(
                    // requireActivity(), Manifest.permission.CAMERA)
                    Log.i("SMS--", "permission denied")
                }
            }

        })


        receiverOTP = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (Constants.ACTION_FOR_SMS == intent.action) {
                    otpDetectionClient.retrieveOTPFromString(intent.getStringExtra("SMS") ?: "")
                        .let {
                            if (it != "0000")
                                textBox.setText(it)
                        }
                }
            }
        }

        // registering broadcast receiver for sms action
        val intent = IntentFilter()
        intent.addAction(Constants.ACTION_FOR_SMS)
        if (!receiverOTP.isOrderedBroadcast)
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiverOTP, intent)

    }

    private fun openDialogBox() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(R.layout.phone_number_dialog_box)
            .show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val textView = dialog.findViewById<EditText>(R.id.textBox)
        val submit = dialog.findViewById<Button>(R.id.submitBTN)

        textView?.setText("+")
        submit?.setOnClickListener {
            sendOTPMessage(textView?.text.toString())
            dialog.dismiss()
        }
    }

    private fun sendOTPMessage(number: String) {

        val messageRequestObject = MessageRequestObject("sms", "code", number)
        val call = APISupport.sendOTP(Constants.SEND_OTP_MESSAGE,
            preference.getAccessToken(),
            messageRequestObject)
        val progress = ProgressDialog(this)
        progress.setTitle("Loading")
        progress.show()
        call?.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                progress.dismiss()
                if (response.isSuccessful) {
                    (response.body() as LinkedTreeMap<*, *>).let {
                        if (it.containsKey("trackingId")) {
                            trackingId = it["trackingId"].toString()
                        }
                    }
                    Toast.makeText(this@OTPValidationActivity,
                        "OTP sent successfully",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@OTPValidationActivity, "OTP failed to send", Toast.LENGTH_SHORT)
                    .show()
                openDialogBox()
            }

        })
    }

    /**
     * Asking permission for sms.
     *
     * @param fragmentPermissionCallback A callback function which tells sets the sms permission granted or not.
     */
    private fun startPermissionRequest(fragmentPermissionCallback: FragmentPermissionCallback) {
        val requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            fragmentPermissionCallback.onGranted(isGranted)
        }
        requestPermission.launch(Manifest.permission.RECEIVE_SMS)
    }

    /**
     * It will get the system generated Key hash.
     *
     * @return The system generated key hash.
     */
    private fun checkKeyHash(): String {
        val appSignatureHelper = AppSignatureHelper(this)
        appSignatureHelper.getAppSignatures().forEach {
            Log.v("sms", "CODE- $it")
            return it
        }
        return ""
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverOTP)
    }
}