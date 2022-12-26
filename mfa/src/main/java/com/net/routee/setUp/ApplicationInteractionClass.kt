package com.net.routee.setUp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings.Secure
import android.widget.Toast
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.io.BaseEncoding
import com.google.gson.internal.LinkedTreeMap
import com.net.routee.database.AppDatabase
import com.net.routee.database.LocationDataClass
import com.net.routee.interfaces.APICallback
import com.net.routee.preference.SharedPreference
import com.net.routee.retrofit.*
import com.net.routee.utils.Constants
import kotlinx.android.synthetic.main.authentication_dialog.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ApplicationInteractionClass(context: Context) : ContextWrapper(context) {
    private val sharedPreference = SharedPreference(this)

    /**
     * Adding the json object string to shared preference of sdk.
     *
     * @param jsonObject - String format of json object.
     */
    fun setInitialConfiguration(jsonObject: String) {
        sharedPreference.setInitialConfiguration(jsonObject)
    }

    /**
     * To check whether we are using using latest configuration
     *
     */
    fun initialize() {
        val configurationObject = JSONObject(sharedPreference.getInitialConfiguration())
        checkAndGenerateDeviceUUID()

        val initializeCallback = object : APICallback {
            override fun apiResult(result: APIResult<*>) {
                if (result is APILoading) {
                    // handle loading 
                } else if (result is APIError || result is APIErrorRes) {
                    // handle error
                } else if (result is APISuccess) {

                    val jsonObject = JSONObject()
                    (result.payload as LinkedTreeMap<*, *>).forEach {
                        jsonObject.put(it.key.toString(), it.value)
                    }
                    if (hasObjects(jsonObject, listOf("version")) && hasObjects(
                            configurationObject,
                            listOf("version")
                        ) && jsonObject.getString("version")
                            .toDouble() > configurationObject.getString("version").toDouble()
                    )
                        sharedPreference.setInitialConfiguration(jsonObject.toString())
                }
            }
        }
        if (hasObjects(configurationObject, listOf("configurationUrl", "applicationUUID", "configurationCallRetries", "configurationCallRetryDelay"))) {
            val configurationUrl = configurationObject.getString("configurationUrl")
            val applicationDetails =
                ApplicationDetails(applicationUUID = configurationObject.getString("applicationUUID"))
            APISupport.postFireBaseDetails(
                configurationUrl, applicationDetails, initializeCallback,
                configurationObject.getString("configurationCallRetries").toDouble(),
                configurationObject.getString("configurationCallRetryDelay").toDouble()
            )
        }
        if (sharedPreference.getAccessToken().isEmpty())
            getAccessToken()
    }

    fun getAccessToken(checkForAccessToken: CheckForAccessToken? = null) {
        if (Constants.applicationId.isEmpty()){
            Toast.makeText(this, "Please add credentials for access token", Toast.LENGTH_SHORT).show()
            return
        }
        val call = APISupport.postAPIForAccessToken(Constants.URL_FOR_ACCESS_TOKEN, "client_credentials", "Basic ${convertToBase64("${Constants.applicationId}:${Constants.applicationSecret}")}")
        val progress = ProgressDialog(this)
        progress.setTitle("Loading")
        progress.show()
        call?.enqueue(object : Callback<Any>{
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                progress.dismiss()
                if (response.isSuccessful) {
                    (response.body() as LinkedTreeMap<*, *>).let {
                        if (it.containsKey("access_token")) {
                            checkForAccessToken?.isObtained(true)
                            sharedPreference.setAccessToken(it["access_token"].toString())
                            Toast.makeText(this@ApplicationInteractionClass, "Got access token", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    checkForAccessToken?.isObtained(false)
                    Toast.makeText(this@ApplicationInteractionClass, "change credentials for access token", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@ApplicationInteractionClass, "change credentials for access token", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun convertToBase64(string: String): String {
        return BaseEncoding.base64().encode(string.toByteArray())
    }

    /**
     * It will set the user id to the sdk.
     *
     * @param userId User id string to store in preference.
     */
    fun setUserId(userId: String) {
        val configurationObject = JSONObject(sharedPreference.getInitialConfiguration())
        if (sharedPreference.getUserId().isEmpty()) {
            tokenChanged(userId, configurationObject)
        }
        sharedPreference.setUserId(userId)
        val userIdCallback = object : APICallback {
            override fun apiResult(result: APIResult<*>) {
                if (result is APILoading) {
                    // handle loading
                } else if (result is APIError || result is APIErrorRes) {
                    // handle error
                } else if (result is APISuccess) {
                    // handle success response
                    result.payload
                }
            }
        }
        if (hasObjects(configurationObject, listOf("fireBaseConsumer", "applicationUUID", "fireBaseConsumerCallRetries", "fireBaseConsumerRetryDelay"))) {
            val firebaseUrl = configurationObject.getString("fireBaseConsumer")
            val deviceUUID = sharedPreference.getDeviceUUID()
            val applicationDetails = ApplicationDetails(
                applicationUUID = configurationObject.getString("applicationUUID"),
                userId = userId,
                deviceUUID = deviceUUID
            )
            APISupport.postFireBaseDetails(
                firebaseUrl,
                applicationDetails,
                userIdCallback,
                configurationObject.getString("fireBaseConsumerCallRetries").toDouble(),
                configurationObject.getString("fireBaseConsumerRetryDelay").toDouble()
            )
        }
    }

    /**
     * It will called once token changed for corresponding user id.
     *
     * @param userId Unique user id.
     * @param configurationObject the object which is stored from client application.
     */
    fun tokenChanged(userId: String, configurationObject: JSONObject) {
        val tokenUpdateCallback = object : APICallback {
            override fun apiResult(result: APIResult<*>) {
                if (result is APILoading) {
                    // handle loading
                } else if (result is APIError || result is APIErrorRes) {
                    // handle error
                } else if (result is APISuccess) {
                    // handle success response
                    result.payload
                }
            }
        }
        if (hasObjects(configurationObject, listOf("fireBaseConsumer", "applicationUUID", "fireBaseConsumerCallRetries", "fireBaseConsumerRetryDelay"))) {
            val token = sharedPreference.getFCMToken()
            val deviceUUID = sharedPreference.getDeviceUUID()
            val applicationDetails = ApplicationDetails(
                applicationUUID = configurationObject.getString("applicationUUID"),
                userId = userId,
                deviceUUID = deviceUUID,
                token = token
            )
            val firebaseUrl = configurationObject.getString("fireBaseConsumer")
            APISupport.postFireBaseDetails(
                firebaseUrl,
                applicationDetails,
                tokenUpdateCallback,
                configurationObject.getString("fireBaseConsumerCallRetries").toDouble(),
                configurationObject.getString("fireBaseConsumerRetryDelay").toDouble()
            )
        }
    }

    /**
     * It will check the device UUID is available if not it will
     * create the device UUID
     */
    @SuppressLint("HardwareIds")
    private fun checkAndGenerateDeviceUUID() {
        if (sharedPreference.getDeviceUUID().isEmpty()) {
            val deviceUUID = Secure.getString(
                this.contentResolver,
                Secure.ANDROID_ID
            )
            sharedPreference.setDeviceUUID(deviceUUID)
        }
    }

    /**
     * Just to check whether the strings are there in object or not
     *
     * @param obj the object to check.
     * @param keys list of string.
     * @return A boolean value which will be returned true if all strings are there in that object.
     */
    private fun hasObjects(obj: JSONObject, keys: List<String>): Boolean {
        var hasValues = true
        keys.forEach {
            if (!obj.has(it)) hasValues = false
        }
        return hasValues
    }

    suspend fun getLocations(): List<LocationDataClass>? {
            val dbHelper = AppDatabase.getInstance(this@ApplicationInteractionClass).let { it?.locationDao() }
            return dbHelper?.getAllLocations()
    }

//    fun clearUserData(context: Context) {
//        sharedPreference.deletePreference()
//        AppDatabase.deleteDB(context)
//    }
    interface CheckForAccessToken {
        fun isObtained(boolean: Boolean)
    }
}