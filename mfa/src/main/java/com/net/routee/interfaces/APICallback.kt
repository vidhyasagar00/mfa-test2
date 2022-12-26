package com.net.routee.interfaces

import com.net.routee.retrofit.APIResult

interface APICallback {
    fun apiResult(result: APIResult<*>)
}