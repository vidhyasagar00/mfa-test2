package com.net.routee.database

class DatabaseHelperImpl(private val locationDao: LocationDao): DatabaseHelper {
    override suspend fun getAllLocation(): List<LocationDataClass> {
        return locationDao.getAllLocations()
    }

    override suspend fun getLastLocation(): LocationDataClass {
        return locationDao.getLastLocation()
    }

    override suspend fun addLocation(locationDataClass: LocationDataClass) {
        locationDao.addLocation(locationDataClass)
    }

    override suspend fun deleteLocation(locationDataClass: LocationDataClass) {
        locationDao.deleteLocation(locationDataClass)
    }

    override suspend fun deleteAllLocations() {
        locationDao.deleteAllLocations()
    }
}