package com.maximosantino.prendida.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PcDeviceDao {

    @Query("SELECT * FROM pc_devices ORDER BY name ASC")
    fun getAllDevices(): Flow<List<PcDeviceEntity>>

    @Insert
    suspend fun insertDevice(device: PcDeviceEntity)

    @Update
    suspend fun updateDevice(device: PcDeviceEntity)

    @Delete
    suspend fun deleteDevice(device: PcDeviceEntity)

    @Query("DELETE FROM pc_devices WHERE id = :deviceId")
    suspend fun deleteById(deviceId: Int)
}