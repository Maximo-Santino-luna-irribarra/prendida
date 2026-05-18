package com.maximosantino.prendida.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pc_devices")
data class PcDeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val macAddress: String,
    val broadcastIp: String,
    val deviceIp: String,
    val port: Int
)