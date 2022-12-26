package com.net.routee.firebaseMessage

import android.app.PendingIntent

/**
 * Action class with icon, name of the string to be placed on notification and corresponding intent on clicking this string.
 *
 * @property icon It is resource id default it was 0.
 * @property actionName The name of action that should present on notification.
 * @property actionIntent It is intent to move corresponding screen.
 */
data class FCMAction(var icon: Int? = null, var actionName: String, var actionIntent: PendingIntent? = null)