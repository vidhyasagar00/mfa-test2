package com.net.routee.utils

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

class PermissionReqHandler(private val activity: ComponentActivity) {
    @RequiresApi(Build.VERSION_CODES.N)
    private var requestPermission: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    false) || permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,
                    false) || permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,
                    false) -> {
                    val intent = Intent("permissionResult")
                    intent.putExtra("result", true)
                    activity.sendBroadcast(intent)
                }
                else -> {
                    val intent = Intent("permissionResult")
                    intent.putExtra("result", false)
                    activity.sendBroadcast(intent)
                }
            }
        }


    fun request(stringArrayExtra: Array<String>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestPermission.launch(stringArrayExtra)
        }
    }

}