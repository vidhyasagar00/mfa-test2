package com.net.routee.preference

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

class SharedPreferenceTest {
    private lateinit var instrumentationContext: Context
    private lateinit var sharedPreference: SharedPreference

    private fun getRandomNumber(): String{
        return "" + Math.random() * (300) + 100
    }
    private fun getNotifyId(): Int {
        //By default the notification ID is 100
        return 100
    }


    private fun saveAccessToken():String{
        val token= getRandomNumber() +"XYZ"
        sharedPreference.setAccessToken(token)
        return token
    }
    private fun saveNotificationAccessToken():String{
        val token= getRandomNumber() +"XYZ"
        sharedPreference.setNotificationAccessToken(token)
        return token
    }


    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        sharedPreference = SharedPreference(instrumentationContext)
    }


    private fun saveChannelId(): String {
        val number = getRandomNumber()
        sharedPreference.saveChannelId(number)
        return number
    }

    @Test
    fun getChannelId() {
        val number = saveChannelId()
        assertTrue(number == sharedPreference.getChannelId())
    }

    @Test
    fun getNotificationId() {
        val notificationId = getNotifyId()
        assertTrue(notificationId == sharedPreference.getNotificationId())
    }

    private fun setSmallIcon(): Int {
        val icon =Math.random().toInt()
        sharedPreference.setSmallIcon(icon)
        sharedPreference.setLargeIcon(icon)
        return icon
    }

    @Test
    fun getSmallIcon() {
        val icon = setSmallIcon()
        assertTrue(icon==sharedPreference.getSmallIcon())
    }


    private fun setLargeIcon(): Int {
        val icon =Math.random().toInt()
        sharedPreference.setSmallIcon(icon)
        sharedPreference.setLargeIcon(icon)
        return icon
    }

    @Test
    fun getLargeIcon() {
        val icon = setLargeIcon()
        assertTrue(icon==sharedPreference.getLargeIcon())
    }

    @Test
    fun getAccessToken() {
        val token =saveAccessToken()
        assertTrue(token==sharedPreference.getAccessToken())
    }

    @Test
    fun setAccessToken() {
    }

    @Test
    fun getNotificationAccessToken() {
        val token =saveNotificationAccessToken()
        assertTrue(token==sharedPreference.getNotificationAccessToken())
    }

    @Test
    fun setNotificationAccessToken() {
    }

    private fun setFCMToken(): String {
        val token= getRandomNumber() +Math.PI+Math.random()
        sharedPreference.setFCMToken(token)
        return  token
    }

    @Test
    fun getFCMToken() {
        val token = setFCMToken()
        assertTrue(token==sharedPreference.getFCMToken())
    }

    private fun setInitialConfiguration(): String {
        val config = getRandomNumber()
        sharedPreference.setInitialConfiguration(config)
        return config
    }

    @Test
    fun getInitialConfiguration() {
        val token = setInitialConfiguration()
        assertTrue(token==sharedPreference.getInitialConfiguration())
    }

    private fun setDeviceUUID(): String {
        val number = getRandomNumber()
        sharedPreference.setDeviceUUID(number)
        return number
    }

    @Test
    fun getDeviceUUID() {
        val deviceID = setDeviceUUID()
        assertTrue(deviceID==sharedPreference.getDeviceUUID())
    }

    private fun setUserId(): String {
        val number = getRandomNumber()
        sharedPreference.setUserId(number)
        return number
    }

    @Test
    fun getUserId() {
        val userID = setUserId()
        assertTrue(userID==sharedPreference.getUserId())
    }

    private fun setPrefixForOTP(): String {
        val number = getRandomNumber()
        sharedPreference.setPrefixForOTP(number)
        return number
    }

    @Test
    fun getPrefixForOTP() {
        val otp = setPrefixForOTP()
        assertTrue(otp==sharedPreference.getPrefixForOTP())
    }

    private fun setNoOfDigitsForOTP(): Int {
        setPrefixForOTP().length.let {
            sharedPreference.setNoOfDigitsForOTP(it)
            return it
        }
    }

    @Test
    fun getNoOfDigitsForOTP() {
        val otpLength = setNoOfDigitsForOTP()
        assertTrue(otpLength==sharedPreference.getNoOfDigitsForOTP())
    }
}