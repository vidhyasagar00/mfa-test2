package com.net.routee.retrofit

sealed class APIResult<out T>
object APILoading : APIResult<Nothing>()
data class APIErrorRes(val messageRes: Int) : APIResult<Nothing>()
data class APIError(val message: String) : APIResult<Nothing>()
data class APISuccess<T>(val payload: T) : APIResult<T>()