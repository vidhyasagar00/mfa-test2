package com.net.routee.location

import com.net.routee.database.LocationDataClass

data class LocationObject(
    val deviceId: String?,
    val version: String?,
    val data: List<LocationDataClass>?
)
