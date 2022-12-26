package com.net.routee.services

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import com.net.routee.R
import com.net.routee.interfaces.AuthCallback
import com.net.routee.preference.SharedPreference
import com.net.routee.retrofit.APISupport
import com.net.routee.setUp.ApplicationDetails
import com.net.routee.utils.Constants
import kotlinx.android.synthetic.main.authentication_dialog.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FCMServices(
    activity: FragmentActivity
) : ContextWrapper(activity) {

    private lateinit var preference: SharedPreference
    private lateinit var configurationObject: JSONObject
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    fun startAuthentication(sampleIntent: Intent?=null) {
        var intent = sampleIntent
        preference = SharedPreference(this)
        configurationObject = JSONObject(preference.getInitialConfiguration())

        if (intent != null) {
            val type = if (intent.hasExtra("authTypes" + ""))
                intent.extras?.getString("authTypes") else ""
            if (type != AuthType.STATUS_UPDATE.type) {
                if (preference.getIsAuthenticationInRow()) {
                    val existingAuthenticationsList: ArrayList<Intent> =
                        preference.getListOfAuthenticationIntents() ?: ArrayList()
                    existingAuthenticationsList.add(intent)
                    preference.setListOfAuthenticationIntents(existingAuthenticationsList)
                    return
                }
            } else {
                val status = intent.extras?.getString("status")
                val token = intent.extras?.getString("actionToken")
                val statusIntent = Intent("videoConferenceStatus")
                statusIntent.putExtra("status", status)
                statusIntent.putExtra("actionToken", token)
                sendBroadcast(statusIntent)
                return
            }
        } else {
            val existingAuthenticationsList: ArrayList<Intent>? =
                preference.getListOfAuthenticationIntents()
            if (existingAuthenticationsList?.size == 0) return

            intent = existingAuthenticationsList?.get(0)
            existingAuthenticationsList?.removeAt(0)
            if (existingAuthenticationsList != null) {
                preference.setListOfAuthenticationIntents(existingAuthenticationsList)
            }
        }

            if (intent?.hasExtra("accessToken") == true) {
            intent.getStringExtra("assessToken")?.let { preference.setNotificationAccessToken(it) }
        }
        if (intent?.hasExtra("authTypes") != true)
            return

        preference.setIsAuthenticationInRow(true)
        val manager = NotificationManagerCompat.from(this)
        manager.cancel(intent.getIntExtra("NOTIFICATION_ID", -1))


        val type = if (intent.hasExtra("authTypes" +
                    "")
        ) intent.extras?.getString("authTypes") else ""





        val category = if (intent.hasExtra("category")) intent.extras?.get("category") else ""
        if (category == AuthCategory.ACTION_CHOICE.type) {

            if (intent.action == getString(R.string.accept) || type == AuthType.VIDEO_CONFERENCE.type) {
                scope.launch {
                    handleAuthScreens(type, intent)
                }


            } else {
                val dialog = AlertDialog.Builder(this)
                    .setView(R.layout.authentication_dialog).show()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
                val title = dialog.findViewById<TextView>(R.id.title)
                val description = dialog.findViewById<TextView>(R.id.description)
                val positiveButton = dialog.findViewById<Button>(R.id.positiveButton)
                val negativeButton = dialog.findViewById<Button>(R.id.negativeButton)

                title.text = intent.extras?.getString("title") ?: "Title"
                description.text = intent.extras?.getString("description") ?: "Description"
                positiveButton.text = getString(R.string.accept)

                negativeButton.text = getString(R.string.deny)

                positiveButton.setOnClickListener {
                    dialog.dismiss()
                    scope.launch {
                        handleAuthScreens(type, intent)
                    }
                }
                negativeButton.setOnClickListener {
                    Authenticator.publishResult(Authenticator.failed, this@FCMServices, Intent())
                    preference.setIsAuthenticationInRow(false)
                    startAuthentication()
                    val applicationDetails =
                        ApplicationDetails(deviceUUID = preference.getDeviceUUID(),
                            applicationUUID = configurationObject.getString("applicationUUID"),
                            userId = preference.getUserId(),
                            actionToken = preference.getNotificationAccessToken(),
                            actionChoice = "2")
                    val callback =
                        APISupport.postAPI(Constants.API_URL_FOR_AUTH_PERMISSIONS,
                            applicationDetails)
                    val progress = ProgressDialog(this)
                    progress.setTitle("Loading")
                    progress.show()
                    callback?.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            progress.dismiss()
//                            if (response.isSuccessful) {
//                                // success response
//                            }
                        }

                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            progress.dismiss()
                            // failure response
                        }

                    })
                    dialog.dismiss()
                }
            }
        }
    }

    private fun handleAuthScreens(type: String? = null, intent: Intent) {

        val authTypes = type?.split(",") ?: arrayListOf()
        val authenticator = Authenticator(applicationContext)
        authenticator.requestAuth(authTypes, intent)
        authenticator.getResponseListener(object : AuthCallback {
            @SuppressLint("SetTextI18n")
            override fun response(success: Boolean) {
                val popup = AlertDialog.Builder(this@FCMServices)
                    .setView(R.layout.success_failute_dialog).show()
                popup.window?.setGravity(Gravity.BOTTOM)
                popup.setCancelable(false)
                popup.setCanceledOnTouchOutside(false)
//                popup.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val btnOk = popup.findViewById<Button>(R.id.btnOk)
                val imgView = popup.findViewById<ImageView>(R.id.imgAuthResult)
                val txtView = popup.findViewById<TextView>(R.id.txtAuthResult)
                btnOk.setOnClickListener {
                    preference.setIsAuthenticationInRow(false)
                    startAuthentication()
                    popup.dismiss()
                }

                if (success) {
                    txtView.text = "Verification Success"
                    imgView.setImageResource(R.drawable.ic_success)
                } else {
                    txtView.text = "Verification Failed"
                    imgView.setImageResource(R.drawable.ic_failure)
                }
            }
        })

    }


    enum class AuthType(val type: String) {
        BIOMETRIC("Biometric"),
        OTP("OTP"),
        LOCATION("Location"),
        PUSH_OTP("PushOTP"),
        VIDEO_CONFERENCE("VideoConference"),
        STATUS_UPDATE("statusUpdate")
    }

    enum class AuthCategory(val type: String) {
        ACTION_CHOICE("ActionChoice")
    }

}