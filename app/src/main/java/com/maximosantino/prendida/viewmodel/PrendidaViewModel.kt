package com.maximosantino.prendida.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maximosantino.prendida.data.PcDeviceDao
import com.maximosantino.prendida.data.PcDeviceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PrendidaViewModel(
    private val pcDeviceDao: PcDeviceDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: Flow<String> = _searchQuery

    val devices: Flow<List<PcDeviceEntity>> = pcDeviceDao.getAllDevices()

    val filteredDevices: Flow<List<PcDeviceEntity>> = devices.combine(_searchQuery) { devices, query ->
        if (query.isBlank()) {
            devices
        } else {
            devices.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addDevice(
        name: String,
        macAddress: String,
        broadcastIp: String,
        deviceIp: String,
        port: Int
    ) {
        viewModelScope.launch {
            val device = PcDeviceEntity(
                name = name.trim(),
                macAddress = macAddress.trim(),
                broadcastIp = broadcastIp.trim(),
                deviceIp = deviceIp.trim(),
                port = port
            )

            pcDeviceDao.insertDevice(device)
        }
    }

    fun deleteDevice(device: PcDeviceEntity) {
        viewModelScope.launch {
            pcDeviceDao.deleteDevice(device)
        }
    }
}
