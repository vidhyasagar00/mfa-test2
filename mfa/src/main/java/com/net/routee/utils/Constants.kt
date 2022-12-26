package com.net.routee.utils

class Constants {
    companion object {
        const val ACTION_FOR_SMS = "ACTION_FOR_SMS"
        const val ACTION_FOR_FCM_TOKEN = "ACTION_FOR_FCM_TOKEN"
        const val FOREGROUND_NOTIFICATION_CONTENT = "FOREGROUND_NOTIFICATION_CONTENT"
        const val API_URL_FOR_AUTH_PERMISSIONS = "https://ksms.amdtelecom.net/mfa/userActionConsumer.php"
        const val DEFAULT_PREFIX_FOR_OTP = "MFA"
        const val DEFAULT_OTP_LENGTH = 4
        const val LOCATION_UPDATE_URL = "https://ksms.amdtelecom.net/aiCloud/loc.php"
        const val SINGLE_LOCATION_UPDATE_URL = "https://ksms.amdtelecom.net/mfa/locVerify.php"
        const val OTP_UPDATE_URL = "https://connect.routee.net/"
        const val SEND_OTP_MESSAGE = "https://connect.routee.net/2step"
        const val URL_FOR_ACCESS_TOKEN = "https://auth.routee.net/oauth/token"
        const val applicationName = "amdTelecom"
        var applicationId = ""
//        var applicationId = "62f5f41b6d259f00019ced6e"
        var applicationSecret = ""
//        var applicationSecret = "NC7BypxgMB"
    }
}