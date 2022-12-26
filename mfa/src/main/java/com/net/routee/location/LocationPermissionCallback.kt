package com.net.routee.location

/**
 * Is was a call back class for location Service
 *
 */
interface LocationPermissionCallback {
    /**
     * This call back function is set and get the boolean of permission granted or not.
     *
     * @param isGranted A boolean value which has information regarding location permission.
     */
    fun onGranted(isGranted: Boolean)

}