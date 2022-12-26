package com.net.routee.services

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

class AuthenticatorTest {
    private lateinit var instrumentationContext: Context
    private lateinit var authenticator: Authenticator
    private var auths = linkedMapOf<String, AuthState>()

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        authenticator = Authenticator(instrumentationContext)
    }
    fun getAuth(types:List<String>):LinkedHashMap<String, AuthState>{
        auths = linkedMapOf()
        types.forEach {
            auths[it] = AuthState.Pending
        }
        authenticator.requestAuth(types)
        return auths
    }

    @Test
    fun getAuths() {
    }

    @Test
    fun setAuths() {
    }

    @Test
    fun getReceiver() {
    }

    @Test
    fun setReceiver() {
    }

    @Test
    fun requestAuth() {
    }

    @Test
    fun removeAuth() {
    }
}