package com.net.routee.biometric

interface BiometricResultCallback {
    /**
     * It will returns status of finger print permission
     *
     * @param biometricFingerPrintStatus A boolean value with with status of permission
     */
    fun permissionGranted(biometricFingerPrintStatus:BiometricFingerPrintStatus)
}