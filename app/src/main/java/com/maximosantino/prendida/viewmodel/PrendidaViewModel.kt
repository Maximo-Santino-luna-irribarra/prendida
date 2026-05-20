package com.maximosantino.prendida.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maximosantino.prendida.data.PcDeviceDao
import com.maximosantino.prendida.data.PcDeviceEntity
import com.maximosantino.prendida.network.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.content.Context
import com.maximosantino.prendida.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

enum class DeviceStatus {
    OFFLINE,
    CHECKING,
    ONLINE
}

class PrendidaViewModel(
    private val pcDeviceDao: PcDeviceDao
) : ViewModel() {

    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: Flow<String> = _searchQuery

    private val _deviceStatuses = MutableStateFlow<Map<Int, DeviceStatus>>(emptyMap())
    val deviceStatuses: StateFlow<Map<Int, DeviceStatus>> = _deviceStatuses.asStateFlow()

    private val activeJobs = mutableMapOf<Int, kotlinx.coroutines.Job>()

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

    fun startCheckingStatus(
        context: android.content.Context,
        deviceId: Int,
        deviceIp: String,
        deviceName: String
    ) {
        // Si ya estamos comprobando este dispositivo, cancelamos el anterior para empezar de nuevo
        // o simplemente retornamos. Dado que el usuario puede querer "reintentar", cancelamos y empezamos.
        stopCheckingStatus(deviceId)

        val job = viewModelScope.launch {
            _deviceStatuses.value = _deviceStatuses.value + (deviceId to DeviceStatus.CHECKING)

            var isOnline = false
            var attempts = 0
            val maxAttempts = 150 // 5 minutos (150 * 2 segundos = 300 segundos)

            try {
                while (!isOnline && attempts < maxAttempts) {
                    isOnline = NetworkUtils.isDeviceReachable(deviceIp)

                    if (isOnline) {
                        _deviceStatuses.value = _deviceStatuses.value + (deviceId to DeviceStatus.ONLINE)
                        _uiEvents.emit("¡$deviceName se ha encendido!")

                        NotificationHelper.notificarPcPrendida(
                            context = context.applicationContext,
                            deviceName = deviceName
                        )

                        return@launch
                    } else {
                        attempts++
                        delay(2000)
                    }
                }

                if (!isOnline) {
                    _deviceStatuses.value = _deviceStatuses.value + (deviceId to DeviceStatus.OFFLINE)
                }
            } finally {
                activeJobs.remove(deviceId)
            }
        }
        activeJobs[deviceId] = job
    }

    fun stopCheckingStatus(deviceId: Int) {
        activeJobs[deviceId]?.cancel()
        activeJobs.remove(deviceId)
        _deviceStatuses.value = _deviceStatuses.value + (deviceId to DeviceStatus.OFFLINE)
    }

    fun checkDeviceOnce(
        context: android.content.Context,
        deviceId: Int,
        deviceIp: String,
        deviceName: String
    ) {
        viewModelScope.launch {
            _deviceStatuses.value = _deviceStatuses.value + (deviceId to DeviceStatus.CHECKING)
            _uiEvents.emit("Comprobando estado de $deviceName...")
            
            val isOnline = NetworkUtils.isDeviceReachable(deviceIp)
            
            if (isOnline) {
                _deviceStatuses.value = _deviceStatuses.value + (deviceId to DeviceStatus.ONLINE)
                _uiEvents.emit("¡$deviceName está online!")
                NotificationHelper.notificarPcPrendida(
                    context = context.applicationContext,
                    deviceName = deviceName
                )
            } else {
                _deviceStatuses.value = _deviceStatuses.value + (deviceId to DeviceStatus.OFFLINE)
                _uiEvents.emit("$deviceName sigue offline.")
            }
        }
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

    fun updateDevice(
        id: Int,
        name: String,
        macAddress: String,
        broadcastIp: String,
        deviceIp: String,
        port: Int
    ) {
        viewModelScope.launch {
            val device = PcDeviceEntity(
                id = id,
                name = name.trim(),
                macAddress = macAddress.trim(),
                broadcastIp = broadcastIp.trim(),
                deviceIp = deviceIp.trim(),
                port = port
            )
            pcDeviceDao.updateDevice(device)
        }
    }

    fun deleteDevice(device: PcDeviceEntity) {
        viewModelScope.launch {
            pcDeviceDao.deleteDevice(device)
        }
    }
}
