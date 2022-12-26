package com.net.routee.otpdetection

interface FragmentPermissionCallback {
    /**
     * This call back function is set and get the boolean of permission granted or not.
     *
     * @param isGranted A boolean value which has information regarding sms permission.
     */
    fun onGranted(isGranted: Boolean)
}