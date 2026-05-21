package com.maximosantino.prendida.wol

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.maximosantino.prendida.data.PrendidaDatabase
import com.maximosantino.prendida.network.NetworkUtils
import com.maximosantino.prendida.utils.NotificationHelper
import com.maximosantino.prendida.viewmodel.DeviceStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class PrendidaService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var periodicJob: Job? = null
    private val activeChecks = mutableMapOf<Int, Job>()

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_WAIT_FOR_ON = "ACTION_WAIT_FOR_ON"
        
        const val EXTRA_DEVICE_ID = "EXTRA_DEVICE_ID"
        const val EXTRA_DEVICE_IP = "EXTRA_DEVICE_IP"
        const val EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Promover a primer plano lo antes posible para evitar cierres en Android 12+
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopService()
            ACTION_WAIT_FOR_ON -> {
                val deviceId = intent.getIntExtra(EXTRA_DEVICE_ID, -1)
                val deviceIp = intent.getStringExtra(EXTRA_DEVICE_IP)
                val deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME)
                if (deviceId != -1 && deviceIp != null && deviceName != null) {
                    waitForDeviceOn(deviceId, deviceIp, deviceName)
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        NotificationHelper.crearCanalNotificaciones(this)
        val notification = NotificationHelper.getForegroundNotification(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.SERVICE_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification)
        }

        startPeriodicChecks()
    }

    private fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startPeriodicChecks() {
        if (periodicJob?.isActive == true) return

        periodicJob = serviceScope.launch {
            val db = PrendidaDatabase.getDatabase(applicationContext)
            val dao = db.pcDeviceDao()

            while (isActive) {
                try {
                    val devices = dao.getAllDevices().first()
                    devices.forEach { device ->
                        // Don't interfere with fast-tracking checks
                        if (DeviceStatusManager.deviceStatuses.value[device.id] != DeviceStatus.CHECKING) {
                            launch {
                                val isOnline = NetworkUtils.isDeviceReachable(device.deviceIp)
                                DeviceStatusManager.updateStatus(
                                    device.id,
                                    if (isOnline) DeviceStatus.ONLINE else DeviceStatus.OFFLINE
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors in periodic check
                }
                delay(30000)
            }
        }
    }

    private fun waitForDeviceOn(deviceId: Int, deviceIp: String, deviceName: String) {
        activeChecks[deviceId]?.cancel()
        activeChecks[deviceId] = serviceScope.launch {
            DeviceStatusManager.updateStatus(deviceId, DeviceStatus.CHECKING)
            
            var attempts = 0
            val maxAttempts = 150 // 5 minutes

            while (isActive && attempts < maxAttempts) {
                val isOnline = NetworkUtils.isDeviceReachable(deviceIp)
                if (isOnline) {
                    DeviceStatusManager.updateStatus(deviceId, DeviceStatus.ONLINE)
                    NotificationHelper.notificarPcPrendida(applicationContext, deviceName)
                    break
                }
                attempts++
                delay(2000)
            }
            
            if (attempts >= maxAttempts) {
                DeviceStatusManager.updateStatus(deviceId, DeviceStatus.OFFLINE)
            }
            activeChecks.remove(deviceId)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
