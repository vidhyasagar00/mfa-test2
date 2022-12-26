package com.net.routee.services

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FCMServicesClientTest {
    private lateinit var instrumentationContext: Context
//    private lateinit var fcmServices: FCMServices
    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    }
    @Test
    fun isOtp() {
        val fcmToken = FCMServices.AuthType.OTP
        Assert.assertTrue(fcmToken.type=="OTP")

    }
    @Test
    fun isBiometric() {
        val fcmToken = FCMServices.AuthType.BIOMETRIC
        Assert.assertTrue(fcmToken.type=="Biometric")

    }

}