package com.net.routee.retrofit

import com.net.routee.interfaces.API
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    var myApi: API
    init{
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .build()

        val retrofit = Retrofit.Builder().baseUrl(API.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        myApi = retrofit.create(API::class.java)
    }
    companion object {
        private var instance: RetrofitClient? = null

        fun getInstance(): RetrofitClient? {
            if (instance == null || API.baseUrlChanged) {
                instance = RetrofitClient()
                API.baseUrlChanged = false
            }
            return instance
        }
    }
}
