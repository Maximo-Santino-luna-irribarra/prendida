package com.maximosantino.prendida.wol

import com.maximosantino.prendida.viewmodel.DeviceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DeviceStatusManager {
    private val _deviceStatuses = MutableStateFlow<Map<Int, DeviceStatus>>(emptyMap())
    val deviceStatuses: StateFlow<Map<Int, DeviceStatus>> = _deviceStatuses.asStateFlow()

    fun updateStatus(deviceId: Int, status: DeviceStatus) {
        _deviceStatuses.value = _deviceStatuses.value + (deviceId to status)
    }

    fun clearStatus(deviceId: Int) {
        _deviceStatuses.value = _deviceStatuses.value - deviceId
    }

    fun clearAllStatuses() {
        _deviceStatuses.value = emptyMap()
    }
}
