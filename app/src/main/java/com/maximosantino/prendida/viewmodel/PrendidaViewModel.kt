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
import android.content.Intent
import com.maximosantino.prendida.utils.NotificationHelper
import com.maximosantino.prendida.wol.DeviceStatusManager
import com.maximosantino.prendida.wol.PrendidaService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

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

    val deviceStatuses: StateFlow<Map<Int, DeviceStatus>> = DeviceStatusManager.deviceStatuses

    val devices: Flow<List<PcDeviceEntity>> = pcDeviceDao.getAllDevices()

    val filteredDevices: Flow<List<PcDeviceEntity>> = devices.combine(_searchQuery) { devices, query ->
        if (query.isBlank()) {
            devices
        } else {
            devices.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    fun startService(context: Context) {
        try {
            val intent = Intent(context, PrendidaService::class.java).apply {
                action = PrendidaService.ACTION_START
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            // Log or ignore if service cannot be started in current state
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
        val intent = Intent(context, PrendidaService::class.java).apply {
            action = PrendidaService.ACTION_WAIT_FOR_ON
            putExtra(PrendidaService.EXTRA_DEVICE_ID, deviceId)
            putExtra(PrendidaService.EXTRA_DEVICE_IP, deviceIp)
            putExtra(PrendidaService.EXTRA_DEVICE_NAME, deviceName)
        }
        context.startService(intent)
    }

    fun stopCheckingStatus(deviceId: Int) {
        // En esta implementación, la cancelación se maneja al iniciar una nueva
        // o podríamos agregar un ACTION_CANCEL_WAIT a PrendidaService.
        // Por ahora lo dejamos así para simplificar.
        DeviceStatusManager.updateStatus(deviceId, DeviceStatus.OFFLINE)
    }

    fun checkDeviceOnce(
        context: android.content.Context,
        deviceId: Int,
        deviceIp: String,
        deviceName: String
    ) {
        viewModelScope.launch {
            DeviceStatusManager.updateStatus(deviceId, DeviceStatus.CHECKING)
            _uiEvents.emit("Comprobando estado de $deviceName...")
            
            val isOnline = NetworkUtils.isDeviceReachable(deviceIp)
            
            if (isOnline) {
                DeviceStatusManager.updateStatus(deviceId, DeviceStatus.ONLINE)
                _uiEvents.emit("¡$deviceName está online!")
                NotificationHelper.notificarPcPrendida(
                    context = context.applicationContext,
                    deviceName = deviceName
                )
            } else {
                DeviceStatusManager.updateStatus(deviceId, DeviceStatus.OFFLINE)
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
