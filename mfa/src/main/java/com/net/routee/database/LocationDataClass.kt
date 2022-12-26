package com.net.routee.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_class")
data class LocationDataClass (
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "version") val version: String?,
    @ColumnInfo(name = "dateTime") val dateTime: String?,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?,
    @ColumnInfo(name = "accuracy") val accuracy: Double?
)