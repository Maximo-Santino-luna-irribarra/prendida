package com.maximosantino.prendida.wol

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object WakeOnLanSender {

    fun send(
        macAddress: String,
        broadcastIp: String,
        deviceIp: String,
        port: Int
    ): Result<String> {
        return try {
            require(port in 1..65535) {
                "El puerto debe estar entre 1 y 65535."
            }

            val cleanMac = cleanMacAddress(macAddress)
            val macBytes = macToBytes(cleanMac)
            val magicPacket = createMagicPacket(macBytes)

            val destinations = listOf(
                broadcastIp,
                "255.255.255.255",
                deviceIp
            )
                .filter { it.isNotBlank() }
                .distinct()

            DatagramSocket().use { socket ->
                socket.broadcast = true
                socket.reuseAddress = true

                destinations.forEach { ip ->
                    val address = InetAddress.getByName(ip)

                    val packet = DatagramPacket(
                        magicPacket,
                        magicPacket.size,
                        address,
                        port
                    )

                    socket.send(packet)
                }
            }

            Result.success("Paquete enviado a: ${destinations.joinToString(", ")}:$port")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cleanMacAddress(macAddress: String): String {
        return macAddress
            .replace("-", "")
            .replace(":", "")
            .replace(" ", "")
            .uppercase()
    }

    private fun macToBytes(cleanMac: String): ByteArray {
        require(cleanMac.length == 12) {
            "La MAC debe tener 12 caracteres hexadecimales."
        }

        require(cleanMac.all { it in '0'..'9' || it in 'A'..'F' }) {
            "La MAC contiene caracteres inválidos."
        }

        return cleanMac.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun createMagicPacket(macBytes: ByteArray): ByteArray {
        val packet = ByteArray(6 + 16 * macBytes.size)

        for (i in 0 until 6) {
            packet[i] = 0xFF.toByte()
        }

        var index = 6

        repeat(16) {
            macBytes.copyInto(
                destination = packet,
                destinationOffset = index
            )
            index += macBytes.size
        }

        return packet
    }
}