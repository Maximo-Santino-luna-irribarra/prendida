package com.maximosantino.prendida.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NetworkUtils {

    fun isConnectedToWifi(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    suspend fun isDeviceReachable(ipAddress: String, timeout: Int = 1500): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val address = java.net.InetAddress.getByName(ipAddress)
                // Primero intentamos con isReachable (ICMP / Echo)
                if (address.isReachable(timeout)) {
                    return@withContext true
                }

                // Si falla, intentamos conectar a puertos comunes (Windows/Linux)
                // Puerto 135 (RPC), 445 (SMB), 22 (SSH), 3389 (RDP)
                val ports = listOf(135, 445, 22, 3389)
                for (port in ports) {
                    try {
                        val socket = java.net.Socket()
                        socket.connect(java.net.InetSocketAddress(ipAddress, port), 500)
                        socket.close()
                        return@withContext true
                    } catch (e: Exception) {
                        // Continuar con el siguiente puerto
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
}