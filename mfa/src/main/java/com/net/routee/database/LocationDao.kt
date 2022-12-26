package com.net.routee.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface LocationDao {

    @Query("SELECT * FROM location_class")
    suspend fun getAllLocations(): List<LocationDataClass>

    @Query("SELECT * FROM location_class ORDER BY id DESC LIMIT 1")
    suspend fun getLastLocation(): LocationDataClass

    @Insert(onConflict = REPLACE)
    suspend fun addLocation(locationDataClass: LocationDataClass)

    @Delete
    suspend fun deleteLocation(locationDataClass: LocationDataClass)

    @Query("DELETE FROM location_class")
    suspend fun deleteAllLocations()

}