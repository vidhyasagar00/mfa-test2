package com.net.routee.interfaces

import com.net.routee.location.LocationObject
import com.net.routee.location.SingleLocationObject
import com.net.routee.otpdetection.MessageRequestObject
import com.net.routee.setUp.ApplicationDetails
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface API {
    companion object {
        var BASE_URL = ""
        var baseUrlChanged = false
    }

    @POST
    fun postConfiguration(
        @Url url: String,
        @Body applicationDetails: ApplicationDetails,
    ): Call<Any>?

    @POST
    fun postJson(
        @Url url: String,
        @Body jsonObject: JSONObject,
    ): Call<ResponseBody>?

    @Headers("content-type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST
    fun postAPIForAccessToken(
        @Header("Authorization") token: String,
        @Url url: String,
        @Field("grant_type") grantType: String,
    ): Call<Any>?

    @POST
    fun postAPIForLocationUpdate(
        @Url url: String,
        @Body locationObject: LocationObject
    ): Call<Any>?

    @POST
    fun postAPIForLocationUpdate(
        @Url url: String,
        @Body singleLocationObject: SingleLocationObject
    ): Call<ResponseBody>?

    @POST
    fun sendOTP(
        @Url url: String,
        @Header("Authorization") accessToken: String,
        @Body messageRequestObject: MessageRequestObject,
    ): Call<Any>?

    @FormUrlEncoded
    @POST("/2step/{trackingId}")
    fun verifyOTP(
        @Header("Authorization") accessToken: String,
        @Path("trackingId") trackingId: String,
        @Field("answer") otp: String,
    ): Call<Any>?


}
