package com.net.routee.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.net.routee.utils.Constants

class SharedPreference(context: Context) {
    private val mainPreference = "amd"
    private var pref: SharedPreferences = context.getSharedPreferences(mainPreference, 0)

    companion object {
        private var instance: SharedPreference? = null
        fun init(context: Context): SharedPreference {

            return instance ?: SharedPreference(context)
        }
    }

//    @SuppressLint("CommitPrefEdits")
//    fun deletePreference() {
//        // need to remove later
////        val fcmToken = getFCMToken()
//
//        pref.edit().clear().apply()
//
//        // need to remove later
////        setFCMToken(fcmToken)
//    }

    /**
     * Save the Channel ID.
     *
     * @param channelId The id to store.
     */
    @SuppressLint("CommitPrefEdits")
    fun saveChannelId(channelId: String) {
        pref.edit().apply {
            putString("channel_id", channelId)
            apply()
        }
    }

    /**
     * gets the Channel ID.
     *
     * @return The channel id which saved earlier
     */
    fun getChannelId(): String {
        return pref.getString("channel_id", "") ?: "default"
    }

    /**
     * get the notification ID.
     *
     * @return The notification id which saved earlier
     */
    fun getNotificationId(): Int {
        //By default the notification ID is 100
        val id = pref.getInt("notify_id", 100)
        setNotificationId(id + 1)
        return id
    }

    /**
     * Set the notification ID.
     *
     * @param id The id to store.
     */
    private fun setNotificationId(id: Int) {
        val i = if (id == 1000) id else 100
        val editor = pref.edit()
        editor.putInt("notify_id", i)
        editor.apply()
    }

    /**
     * set the small Icon.
     *
     * @param id The resource id to save.
     */
    fun setSmallIcon(id: Int) {
        val editor = pref.edit()
        editor.putInt("smallIcon", id)
        editor.apply()
    }

    /**
     * get the small Icon.
     *
     * @return The resource id of icon which saved earlier.
     */
    fun getSmallIcon(): Int {
        return pref.getInt("smallIcon", 0)
    }

    /**
     * set the large Icon.
     *
     * @param id The resource id to save.
     */
    fun setLargeIcon(id: Int) {
        val editor = pref.edit()
        editor.putInt("largeIcon", id)
        editor.apply()
    }

    /**
     * get the large ID.
     *
     * @return The resource id of icon which saved earlier.
     */
    fun getLargeIcon(): Int {
        return pref.getInt("largeIcon", 0)
    }

    /**
     * set the FCM Token
     *
     * @param token The token to save
     */
    fun setFCMToken(token: String) {
        if (token != getFCMToken()) {
            val editor = pref.edit()
            editor.putString("token", token)
            editor.apply()
        }
    }

    /**
     * get the Access Token
     *
     * @return The saved FCM token
     */
    fun getAccessToken(): String {
        return pref.getString("accessToken", "") ?: ""
    }
    /**
     * set the Access Token
     *
     * @param token The token to save
     */
    fun setAccessToken(token: String) {
        if (token != getFCMToken()) {
            val editor = pref.edit()
            editor.putString("accessToken", token)
            editor.apply()
        }
    }
    /**
     * get the Access Token
     *
     * @return The saved FCM token
     */
    fun getNotificationAccessToken(): String {
        return pref.getString("accessToken", "") ?: ""
    }
    /**
     * set the Access Token
     *
     * @param token The token to save
     */
    fun setNotificationAccessToken(token: String) {
        if (token != getFCMToken()) {
            val editor = pref.edit()
            editor.putString("accessToken", token)
            editor.apply()
        }
    }

    /**
     * get the FCM Token
     *
     * @return The saved FCM token
     */
    fun getFCMToken(): String {
        return pref.getString("token", "") ?: ""
    }

    fun setInitialConfiguration(jsonObject: String) {
        val editor = pref.edit()
        editor.putString("configurationObject", jsonObject)
        editor.apply()
    }

    fun getInitialConfiguration(): String {
        return pref.getString("configurationObject", "{}") ?: "{}"
    }

    fun setDeviceUUID(deviceUUID: String) {
        val editor = pref.edit()
        editor.putString("deviceUUID", deviceUUID)
        editor.apply()
    }

    fun getDeviceUUID(): String {
        return pref.getString("deviceUUID", "") ?: ""
    }

    fun setUserId(userId: String) {
        val editor =  pref.edit()
        editor.putString("userId", userId)
        editor.apply()
    }

    fun getUserId(): String {
        return pref.getString("userId", "") ?: ""
    }

    fun setPrefixForOTP(prefix: String) {
        val editor =  pref.edit()
        editor.putString("prefix", prefix)
        editor.apply()
    }

    fun getPrefixForOTP(): String {
        return pref.getString("prefix", Constants.DEFAULT_PREFIX_FOR_OTP) ?: Constants.DEFAULT_PREFIX_FOR_OTP
    }

    fun setNoOfDigitsForOTP(otpCount: Int) {
        val editor =  pref.edit()
        editor.putInt("otpCount", otpCount)
        editor.apply()
    }

    fun getNoOfDigitsForOTP(): Int {
        return pref.getInt("otpCount", Constants.DEFAULT_OTP_LENGTH)
    }

    /**
     * TODO - Need to Remove
     *
     * @param boolean
     */
    fun setLocationServiceOn(boolean: Boolean) {
        val editor =  pref.edit()
        editor.putBoolean("locationServiceOn", boolean)
        editor.apply()
    }

    fun isLocationServiceOn(): Boolean {
        return pref.getBoolean("locationServiceOn",false)
    }

    fun setIsAuthenticationInRow(isAuthenticationInRow: Boolean) {
        val editor =  pref.edit()
        editor.putBoolean("isAuthenticationInRow", isAuthenticationInRow)
        editor.apply()
    }

    fun getIsAuthenticationInRow(): Boolean {
        return pref.getBoolean("isAuthenticationInRow", false)
    }

    fun setListOfAuthenticationIntents(intentList : List<Intent>) {
        val editor =  pref.edit()
        editor.putString("listOfAuthIntents", Gson().toJson(intentList))
        editor.apply()
    }

    fun getListOfAuthenticationIntents(): ArrayList<Intent>? {
        val gson = Gson()
        val jsonString = pref.getString("listOfAuthIntents", null)
        if(jsonString != null) {
            return gson.fromJson(jsonString,
                object : TypeToken<ArrayList<Intent>>() {}.type)
        }
        return null
    }

     fun storeStatusUpdate(status: String?, actionToken:String?) {
        val editor =  pref.edit()
         try {
             editor.putString("status", status)
             editor.putString("actionToken", actionToken)
             editor.apply()
         } catch (e: Exception) {
             // ignored
         }
    }

    fun getStoreStatusUpdate(): Pair<String?, String?> {
        val pair = Pair(pref.getString("status", ""), pref.getString("actionToken", ""))
        storeStatusUpdate(null, null)
        return pair
    }
}