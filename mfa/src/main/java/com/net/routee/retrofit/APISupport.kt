package com.net.routee.retrofit

import com.net.routee.interfaces.API
import com.net.routee.interfaces.APICallback
import com.net.routee.location.LocationObject
import com.net.routee.location.SingleLocationObject
import com.net.routee.otpdetection.MessageRequestObject
import com.net.routee.setUp.ApplicationDetails
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.net.URL

object APISupport {
    fun postFireBaseDetails(
        stringUrl: String,
        applicationDetails: ApplicationDetails,
        apiCallback: APICallback,
        retries: Double,
        sleepDuration: Double,
    ) {
        try {
            apiCallback.apiResult(APILoading)
            val call = postAPI(
                stringUrl,
                applicationDetails
            )
            call?.enqueue(object : CustomizedCallback<Any>(retries, sleepDuration) {
                override fun onResponse(
                    call: Call<Any>,
                    response: Response<Any>,
                ) {
                    if (response.isSuccessful) {
                        apiCallback.apiResult(APISuccess(response.body()))
                    } else {
                        apiCallback.apiResult(APIErrorRes(response.code()))
                        super.onResponse(call, response, apiCallback)
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    super.onFailure(call, t, apiCallback)
                }
            })
        } catch (e: Exception) {
            e.message?.let { apiCallback.apiResult(APIError(it)) }
        }
    }

    fun postAPI(stringUrl: String, applicationDetails: ApplicationDetails): Call<Any>? {
        val url = getURL(stringUrl)
        return RetrofitClient.getInstance()?.myApi?.postConfiguration(
            url,
            applicationDetails
        )
    }
    fun postJson(stringUrl: String, jsonObject: JSONObject): Call<ResponseBody>? {
        val url = getURL(stringUrl)
        return RetrofitClient.getInstance()?.myApi?.postJson(
            url,
            jsonObject
        )
    }

    fun postAPIForAccessToken(stringUrl: String, grantType: String, token: String): Call<Any>? {
        val url = getURL(stringUrl)
        return RetrofitClient.getInstance()?.myApi?.postAPIForAccessToken(
            token,
            url,
            grantType
        )
    }


    /**
     * Generates base url from main url.
     *
     * @param stringUrl the main URL.
     * @return The base url from main URL.
     */
    private fun getURL(stringUrl: String): String {
        val url = URL(stringUrl)
        (url.protocol + "://" + url.authority + "/").let {
            if (it !== API.BASE_URL) {
                API.baseUrlChanged = true
                API.BASE_URL = it
            }
        }

        return url.file

    }

    fun postAPIForLocationUpdate(stringUrl: String, locationObject: LocationObject): Call<Any>? {
        val url = getURL(stringUrl)
        return RetrofitClient.getInstance()?.myApi?.postAPIForLocationUpdate(
            url,
            locationObject
        )
    }

    fun postAPIForLocationUpdate(stringUrl: String, singleLocationObject: SingleLocationObject): Call<ResponseBody>? {
        val url = getURL(stringUrl)
        return RetrofitClient.getInstance()?.myApi?.postAPIForLocationUpdate(
            url,
            singleLocationObject
        )
    }

    fun sendOTP(
        stringUrl: String,
        accessToken: String,
        messageRequestObject: MessageRequestObject,
    ): Call<Any>? {
        val url = getURL(stringUrl)
        return RetrofitClient.getInstance()?.myApi?.sendOTP(
            url,
            "Bearer $accessToken",
            messageRequestObject
        )
    }

    fun verifyOTP(stringUrl: String, accessToken: String, trackingId: String, otp: String): Call<Any>? {
        val url = getURL(stringUrl)
        return RetrofitClient.getInstance()?.myApi?.verifyOTP(
            "Bearer $accessToken",
            trackingId,
            otp
        )
    }


}