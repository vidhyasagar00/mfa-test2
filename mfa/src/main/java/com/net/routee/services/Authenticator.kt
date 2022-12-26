package com.net.routee.services

import android.content.*
import com.net.routee.interfaces.AuthCallback
import com.net.routee.preference.SharedPreference
import com.net.routee.retrofit.APISupport
import com.net.routee.setUp.ApplicationDetails
import com.net.routee.utils.Constants
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class AuthState {
    Pending,
    Success,
    Failed
}

class Authenticator(val context : Context) : ContextWrapper(context){
    var auths = linkedMapOf<String, AuthState>()
    var receiver : BroadcastReceiver? = null
    var authCallBack : AuthCallback? = null

    fun getResponseListener(authCallBack: AuthCallback) {
        this.authCallBack = authCallBack
    }

    fun requestAuth(types: List<String>, intent: Intent){
        auths = linkedMapOf()
        types.forEach {
            auths[it] = AuthState.Pending
        }
        startPendingAuth(intent)
    }

    private fun startPendingAuth(intent: Intent){
        var overAllAuth : AuthState? = null
        auths.keys.forEach {
            if(auths[it] == AuthState.Pending){
                overAllAuth = AuthState.Pending
                startAuth(it, intent)
                return
            } else if (auths[it] == AuthState.Failed){
                overAllAuth = AuthState.Failed
                sendActionChoice(false)
            }
        }
        if(overAllAuth==null) {
            publishSuccess()
        }
    }

    private fun publishSuccess(){
        receiver?.let {
            unregisterReceiver(it)
        }
        sendActionChoice(true)
    }
    private fun sendActionChoice(success: Boolean) {
        val preference = SharedPreference(this)
        val configurationObject = JSONObject(preference.getInitialConfiguration())
        val applicationDetails = ApplicationDetails(deviceUUID = preference.getDeviceUUID(),
            applicationUUID = configurationObject.getString("applicationUUID"),
            userId = preference.getUserId(),
            actionToken = preference.getNotificationAccessToken(),
            actionChoice = if (success) "1" else "2")
        val callback = APISupport.postAPI(Constants.API_URL_FOR_AUTH_PERMISSIONS,
            applicationDetails)
//        val progress = ProgressDialog(context)
//        progress.setTitle("Loading")
//        progress.show()

        callback?.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
//                progress.dismiss()
                var isSuccess = true
                var isPending = false
                auths.keys.forEach {
                    if (auths[it] != AuthState.Success && isSuccess) {
                        isSuccess = false
                    }
                    else if (auths[it] == AuthState.Pending && !isPending)
                        isPending = true
                }
                if (!isPending)
                    authCallBack?.response(isSuccess)
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
//                progress.dismiss()
                // failure response
//                txtView.text = "Verification Failed"
//                imgView.setImageResource(R.drawable.ic_failure)
            }

        })
    }
    private fun startAuth(authType : String, extrasIntent: Intent?){
         receiver?.let {
            unregisterReceiver(it)
        }
        val intent = Intent("ScreenType")
        extrasIntent?.let { intent.putExtras(it) }
        intent.putExtra("screenType", authType)
        sendBroadcast(intent)
        receiver = object : BroadcastReceiver(){
            override fun onReceive(cxt: Context?, res: Intent?) {
                res?.let {
                    res.action?.let {
                        if(res.action == "ScreenAuthRes"){
                            val status = res.getStringExtra("status")
                            if(status == success){
                                auths[authType] = AuthState.Success
                            } else if(status == failed){
                                auths[authType] = AuthState.Failed
                                removeAuth()
                            }
                            startPendingAuth(res)

                        }
                    }
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction("ScreenAuthRes")
        registerReceiver(receiver, filter)
    }

    fun removeAuth(){
//        auths = linkedMapOf()
        receiver?.let {
            unregisterReceiver(it)
        }
    }

    companion object {
        var success = "Success"
        var failed ="Failed"
        fun publishResult(status : String, context: Context, extrasIntent: Intent){
            val intent = Intent("ScreenAuthRes")
            intent.putExtras(extrasIntent)
            intent.putExtra("status", status)
            context.sendBroadcast(intent)
        }
    }
}