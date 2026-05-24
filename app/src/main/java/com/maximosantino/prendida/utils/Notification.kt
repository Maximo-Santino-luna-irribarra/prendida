package com.maximosantino.prendida.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.maximosantino.prendida.R

object NotificationHelper {

    const val SERVICE_NOTIFICATION_ID = 1001
    private const val CHANNEL_ID = "prendida_estado_pc"
    private const val CHANNEL_NAME = "Estado de la PC"

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low priority for the persistent one
            ).apply {
                description = "Notificaciones de estado y monitoreo"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(canal)
        }
    }

    fun getForegroundNotification(context: Context): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_foreground)
            .setContentTitle("Prendida está activo")
            .setContentText("Monitoreando el estado de tus dispositivos")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    fun notificarPcPrendida(context: Context, deviceName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permiso != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_foreground)
            .setContentTitle("¡PC Encendida!")
            .setContentText("Tu equipo '$deviceName' ya está en línea.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Aumentamos la prioridad para que sea más visible
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sonido y vibración por defecto
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notificacion)
    }
}