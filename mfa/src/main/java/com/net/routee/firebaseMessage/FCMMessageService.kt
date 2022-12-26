package com.net.routee.firebaseMessage


import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.net.routee.R
import com.net.routee.preference.SharedPreference
import com.net.routee.retrofit.APISupport
import com.net.routee.services.FCMServices
import com.net.routee.services.NotificationReceiver
import com.net.routee.setUp.ApplicationInteractionClass
import com.net.routee.utils.Constants
import org.json.JSONObject


open class FCMMessageService : FirebaseMessagingService() {

    private lateinit var tokenWatcher: BroadcastReceiver

    /**
     * It was a default function to capture the refreshed and on creation.
     *
     * @param token Refreshed token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val preference = SharedPreference(applicationContext)
        preference.setFCMToken(token)

        val configurationObject = JSONObject(preference.getInitialConfiguration())
        val userId = preference.getUserId()
        if (configurationObject.length() > 0 && userId.isNotEmpty()) {
            ApplicationInteractionClass(applicationContext).tokenChanged(userId,
                configurationObject)
        }


        // adding a broad cast receiver to send token to the application.
        val intent = Intent(Constants.ACTION_FOR_FCM_TOKEN)
        intent.putExtra("FCM_TOKEN", preference.getFCMToken())
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(intent)
    }


    /**
     * For calling this method android version needs to be above android oreo
     * the versions below oreo need not to be registered.
     *
     * @param context This is required to build notification.
     * @param channelName Any string type value to set notification channel name.
     * @param channelId Any string type value to set notification channel id.
     * @param description Any string type value to set notification channel description.
     * @param smallIcon It is a resource type which is to show a small icon top left corner of notification.
     * @param largeIcon It is a resource type which is to show a large icon on right side of notification.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun registerChannel(
        context: Context,
        channelName: String,
        channelId: String,
        description: String = "",
        smallIcon: Int = 0,
        largeIcon: Int = 0,
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val mChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        mChannel.description = description
        mChannel.enableLights(true)
        mChannel.lightColor = Color.RED
        val preference = SharedPreference.init(context)
        preference.saveChannelId(channelId)
        notificationManager.createNotificationChannel(mChannel)
        preference.setSmallIcon(smallIcon)
        preference.setLargeIcon(largeIcon)
    }


    /**
     * This is function that needed to be call from onMessageReceived.
     *
     * @param remoteMessage This is RemoteMessage type object which will be listened onMessageReceive function.
     * @param notificationIntent This is the action where this will be performed on clicking notification.
     * @param actions This is set of FCMAction, for showing the user selection type notification.
     */
    fun handleMessage(
        remoteMessage: RemoteMessage,
        notificationIntent: Intent,
        actions: ArrayList<FCMAction> = arrayListOf(),
    ) {
        sendNotification(remoteMessage.data, notificationIntent, actions)
    }

    /**
     * Here the notification will be popped up based on the Json.
     *
     * @param data This has the Map<String, String?> of strings which were from Json.
     * @param notificationIntent This is the action where this will be performed on clicking notification.
     * @param actions This is set of FCMAction, for showing the user selection type notification.
     */
    private fun sendNotification(
        data: Map<String, String?>,
        notificationIntent: Intent,
        actions: ArrayList<FCMAction>,
    ) {


        val call = APISupport.postJson(Constants.API_URL_FOR_AUTH_PERMISSIONS, JSONObject(data))

        call?.execute()

        val preference = SharedPreference.init(applicationContext)
        notificationIntent.action = System.currentTimeMillis().toString()
        for (key in data.keys) {
            notificationIntent.putExtra(key, data[key])
        }
        notificationIntent.putExtra("is_gcm", true)
        notificationIntent.putExtra("NOTIFICATION_ID", preference.getNotificationId())
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationIntent.action = getString(R.string.accept)
        val pendingIntentForAction = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
        val i = Intent(applicationContext, NotificationReceiver::class.java)
        i.putExtra("NOTIFICATION_ID", preference.getNotificationId())
        val closeIntent = PendingIntent.getBroadcast(
            this,
            0,
            i,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Initializing the notification manager
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Initializing the notification builder
        val builder =
            NotificationCompat.Builder(
                applicationContext, preference.getChannelId()
            )
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentTitle(data["title"] ?: "")
                .setContentText(data["description"] ?: "")
                .setSmallIcon(preference.getSmallIcon())
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        applicationContext.resources,
                        preference.getLargeIcon()
                    )
                )
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true)


        // If the type was invite then actions we are showing in user device
        if (data["category"] == FCMServices.AuthCategory.ACTION_CHOICE.type)
            for (item in actions) {
                builder.addAction(item.icon ?: 0,
                    item.actionName,
                    if (item.actionName == getString(R.string.accept)) pendingIntentForAction else closeIntent)
            }

        // Checking the OS versions and based on that notification was notified
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (isAppForeground(applicationContext)) {
                // adding a broad cast receiver to send token to the application.
                val intent = Intent(Constants.FOREGROUND_NOTIFICATION_CONTENT)
                intent.putExtras(notificationIntent)
                LocalBroadcastManager.getInstance(applicationContext)
                    .sendBroadcast(intent)
            } else {
                if (data.keys.contains("authTypes") && data["authTypes"] == FCMServices.AuthType.STATUS_UPDATE.type) {
                    preference.storeStatusUpdate(data["status"], data["actionToken"])
                } else {
                    preference.getNotificationId().let {
                        notificationManager.notify(
                            it,
                            builder.build()
                        )
                    }
                }
            }
        } else {
            builder
                .setContentInfo(data["description"] ?: "")
                .setNumber(1)
                .setColor(applicationContext.getColor(R.color.notification_color))
                .setWhen(System.currentTimeMillis())

            preference.getNotificationId().let {
                if (isAppForeground(applicationContext) /*&& notificationIntent.hasExtra("biometric")*/) {
                    // adding a broad cast receiver to send token to the application.
                    val intent = Intent(Constants.FOREGROUND_NOTIFICATION_CONTENT)
                    intent.putExtras(notificationIntent)
                    LocalBroadcastManager.getInstance(applicationContext)
                        .sendBroadcast(intent)
                } else {
                    if (data.keys.contains("authTypes") && data["authTypes"] == FCMServices.AuthType.STATUS_UPDATE.type) {
                        preference.storeStatusUpdate(data["status"], data["actionToken"])
                    } else {
                        notificationManager.notify(
                            it,
                            builder.build()
                        )
                    }
                }
            }
        }
    }

    /**
     * It registers the receiver and listens the token.
     *
     * @param fcmTokenCallback It is to send token to application.
     * @param context It is required to register receiver.
     */
    fun startReceiver(fcmTokenCallback: FCMTokenCallback, context: Context) {
        tokenWatcher = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (Constants.ACTION_FOR_FCM_TOKEN == intent.action) {
                    fcmTokenCallback.getToken(intent.getStringExtra("FCM_TOKEN") ?: "")
                } else if (Constants.FOREGROUND_NOTIFICATION_CONTENT == intent.action) {
                    fcmTokenCallback.passIntentExtras(intent)
                }
            }
        }

        // registering broadcast receiver for FCM token
        val intent = IntentFilter()
        intent.addAction(Constants.ACTION_FOR_FCM_TOKEN)
        intent.addAction(Constants.FOREGROUND_NOTIFICATION_CONTENT)
        if (!tokenWatcher.isOrderedBroadcast)
            LocalBroadcastManager.getInstance(context)
                .registerReceiver(tokenWatcher, intent)
    }

    /**
     * It was to unregister the receiver.
     *
     * @param context It is required to unregister receiver.
     */
    fun destroyReceiver(context: Context) {
        if (::tokenWatcher.isInitialized)
            LocalBroadcastManager.getInstance(context).unregisterReceiver(tokenWatcher)
    }

    private fun isAppForeground(context: Context): Boolean {
        val mActivityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val l = mActivityManager.runningAppProcesses
        for (info in l) {
            if (info.uid == context.applicationInfo.uid && info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }
}
