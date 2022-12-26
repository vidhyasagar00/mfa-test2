package com.net.routee.otpdetection

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class OTPDetectionClientTest {
    private lateinit var instrumentationContext: Context

    /**
     * These are the unit tests added
     * 1. Checking the length of Key hash for testing our side
     * 2. Just check the message before receiving
     * 3. Checking OTP splitting without OTP in a string
     * 4. Checking OTP splitting with OTP in a string
     */

    /**
     * getting context from InstrumentationRegistry
     */
    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    }

    /**
     * 1. Checking the length of Key hash for testing our side
     */
    @Test
    fun testKeyHashLength() {
        val appSignatureHelper = AppSignatureHelper(instrumentationContext)
        appSignatureHelper.getAppSignatures().forEach {
            assertTrue(it.length == 11)
        }
    }

    /**
     * 2. Just check the message before receiving
     */
    @Test
    fun checkingMessageWithOutReceiving() {
        val client = OTPDetectionClient(instrumentationContext)
        client.startSMSRetriever()
        assertTrue(client.otpMessage.value == "")
    }

    /**
     * 3. Checking OTP splitting without OTP in a string
     */
    @Test
    fun checkIfNoOTPFoundInMessage() {
        val client = OTPDetectionClient(instrumentationContext)
        client.startSMSRetriever()
        val type = "Hello! there was no OTP."
        assertThat(client.retrieveOTPFromString(type)).isEqualTo("0000")
    }

    /**
     * 4. Checking OTP splitting with OTP in a string
     */
    @Test
    fun splittingOTPFromStrings() {
        val client = OTPDetectionClient(instrumentationContext)
        client.startSMSRetriever()
        val typeOne = "Your OTP is 1212 dJ+1PrgPP5M"
        val typeTwo = "Your 122 34 is 1225 123"
        val typeThree = "Your 12222 1225 123"
        val typeFour = "9994"
        assertThat(client.retrieveOTPFromString(typeOne)).isEqualTo("1212")
        assertThat(client.retrieveOTPFromString(typeTwo)).isEqualTo("1225")
        assertThat(client.retrieveOTPFromString(typeThree)).isEqualTo("1225")
        assertThat(client.retrieveOTPFromString(typeFour)).isEqualTo("9994")
    }
}