package com.net.routee.retrofit

import com.net.routee.interfaces.APICallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToLong

abstract class CustomizedCallback<Any>(
    private var noOfRetries: Double,
    private val sleepDuration: Double
) :
    Callback<Any> {
    private var retries = 0

    fun onFailure(call: Call<Any>, t: Throwable, apiCallback: APICallback) {
        t.message?.let { retryAPI(call, apiCallback, it) }
    }

    fun onResponse(call: Call<Any>, response: Response<Any>, apiCallback: APICallback) {
        if (!response.isSuccessful) {
            retryAPI(call, apiCallback, response.code().toString() +" "+ response.message())
        }
    }

    private fun retryAPI(call: Call<Any>, apiCallback: APICallback, errorMessage: String) {
        if (retries++ < noOfRetries) {
            Thread.sleep((sleepDuration * 1000).roundToLong())
            call.clone().enqueue(this)
        }
        else {
            apiCallback.apiResult(APIError(errorMessage))
        }

    }


}