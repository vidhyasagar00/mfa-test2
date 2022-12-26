package com.net.routee.utils

import android.content.*

class PermissionReqCreator(context: Context) : ContextWrapper(context) {
    var receiver : BroadcastReceiver? = null

    fun requestPermission(req: Array<String>, callback : PermissionCallback){
        val intent = Intent("PermissionSingle")
        intent.putExtra("req", req)
        sendBroadcast(intent)
        receiver?.let {
            unregisterReceiver(it)
        }
        receiver = object : BroadcastReceiver(){
            override fun onReceive(cxt: Context?, res: Intent?) {
                res?.let {
                    res.action?.let {
                        if(res.action == "permissionResult"){
                            val status = res.getBooleanExtra("result",false)
                            callback.onPermissionGranted(status)
                            cancelReqCallback()
                        }
                    }
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction("permissionResult")
        registerReceiver(receiver, filter)
    }


    private fun cancelReqCallback(){
        unregisterReceiver(receiver)
    }

    interface PermissionCallback {
        fun onPermissionGranted(granted : Boolean)
    }
}