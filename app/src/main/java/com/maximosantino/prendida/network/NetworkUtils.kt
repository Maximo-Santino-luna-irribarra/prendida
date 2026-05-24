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

    fun calculateBroadcastAddress(ipAddress: String, subnetMask: String): String? {
        return try {
            val ipParts = ipAddress.split(".").map { it.toInt() }
            val maskParts = subnetMask.split(".").map { it.toInt() }

            if (ipParts.size != 4 || maskParts.size != 4) return null

            val broadcastParts = IntArray(4)
            for (i in 0 until 4) {
                broadcastParts[i] = ipParts[i] or (maskParts[i].inv() and 0xFF)
            }

            broadcastParts.joinToString(".")
        } catch (e: Exception) {
            null
        }
    }

    fun inferSubnetMask(ipAddress: String, broadcastIp: String): String {
        return try {
            val ipParts = ipAddress.split(".").map { it.toInt() }
            val broadcastParts = broadcastIp.split(".").map { it.toInt() }

            if (ipParts.size != 4 || broadcastParts.size != 4) return "255.255.255.0"

            val maskParts = IntArray(4)
            for (i in 0 until 4) {
                // Si el byte del broadcast es igual al del IP, la máscara es 255
                // Si el byte del broadcast es 255 y el del IP no, la máscara es 0 (o parcial)
                if (ipParts[i] == broadcastParts[i]) {
                    maskParts[i] = 255
                } else {
                    // Aproximación simple: si son distintos, asumimos que el host empieza aquí
                    maskParts[i] = 255 - (broadcastParts[i] xor ipParts[i]).let { 
                        // Encontrar la potencia de 2 más cercana hacia arriba
                        var v = it
                        v = v or (v shr 1)
                        v = v or (v shr 2)
                        v = v or (v shr 4)
                        v
                    }
                }
            }
            maskParts.joinToString(".")
        } catch (e: Exception) {
            "255.255.255.0"
        }
    }
}