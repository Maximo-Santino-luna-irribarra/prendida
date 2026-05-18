package com.maximosantino.prendida.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PcDeviceEntity::class],
    version = 1
)
abstract class PrendidaDatabase : RoomDatabase() {

    abstract fun pcDeviceDao(): PcDeviceDao

    companion object {
        @Volatile
        private var INSTANCE: PrendidaDatabase? = null

        fun getDatabase(context: Context): PrendidaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrendidaDatabase::class.java,
                    "prendida_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}