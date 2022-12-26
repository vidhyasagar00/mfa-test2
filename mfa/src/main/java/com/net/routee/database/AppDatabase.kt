package com.net.routee.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocationDataClass::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun locationDao(): LocationDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val dbName = "location.db"


        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, dbName).fallbackToDestructiveMigration().allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
        fun deleteDB(context: Context) {
            context.applicationContext.deleteDatabase(dbName)
        }
    }
}
