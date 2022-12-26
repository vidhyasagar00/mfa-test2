package com.net.routee.firebaseMessage

import android.content.Intent

/**
 * A call back function for the fcm token
 *
 */
interface FCMTokenCallback {
    /**
     * The token will set on onNewIntent and get in broad cast receiver
     *
     * @param token It was a unique firebase token
     */
    fun getToken(token: String)

    fun passIntentExtras(intent: Intent)
}