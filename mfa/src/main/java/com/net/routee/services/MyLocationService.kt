package com.net.routee.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.net.routee.location.LocationClient
import com.net.routee.location.LocationPermissionCallback
import com.net.routee.preference.SharedPreference

class MyLocationService : Service() {

    private lateinit var locationClient : LocationClient

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startForeground(settings : Settings){
        val pendingIntent: PendingIntent =
            Intent(this, settings.type).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        val notification: Notification = NotificationCompat.Builder(this, locationChannel)
            .setContentTitle(settings.title)
            .setContentText(settings.description)
            .setSmallIcon(settings.icon)
            .setContentIntent(pendingIntent)
            .build()

// Notification ID cannot be 0.
        startForeground(location_notification_id, notification)
    }



    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(!::locationClient.isInitialized)
            locationClient = LocationClient(this, object: LocationPermissionCallback{
                override fun onGranted(isGranted: Boolean) {
                    settings?.let {
                        startForeground(it)
                        SharedPreference(applicationContext).setLocationServiceOn(isGranted)
                    }
                }

            })
//        locationClient.isPermissionGranted()
        if(intent.action == start){
            locationClient.startLocationTracking()
        } else if(intent.action == stop){
            locationClient.stopLocationTracking()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                stopForeground(true)
            stopService(Intent(applicationContext,MyLocationService::class.java))
        }


        //collectLocation(this)
        return super.onStartCommand(intent, flags, startId)
    }



    companion object {
        //    SessionManagement sessionManagement;
        // The minimum distance to change Updates in meters
//        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.001 // 10 meters

        // The minimum time between updates in milliseconds
//        private const val MIN_TIME_BW_UPDATES = (100).toLong()

        const val locationChannel = "location_channel"

        const val location_notification_id = 11

        const val start = "START"

        const val stop = "STOP"
        var settings : Settings? = null
        fun startService(context:Context){
            val intent = Intent(context,MyLocationService::class.java)
            intent.action = start
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent)
            else context.startService(intent)
        }
        fun stopService(context:Context){
            val intent = Intent(context,MyLocationService::class.java)
            intent.action = stop
            context.startService(intent)
        }
    }

    data class Settings(val title:String,val description : String, val icon : Int, val type : Class<*>?)
}