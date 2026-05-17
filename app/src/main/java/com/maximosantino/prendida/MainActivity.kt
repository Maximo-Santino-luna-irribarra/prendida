package com.maximosantino.prendida

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.maximosantino.prendida.network.NetworkUtils
import com.maximosantino.prendida.wol.WakeOnLanSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("Mi PC") }
    var macAddress by remember { mutableStateOf("74-56-3C-54-45-D7") }
    var broadcastIp by remember { mutableStateOf("192.168.1.255") }
    var deviceIp by remember { mutableStateOf("192.168.1.54") }
    var portText by remember { mutableStateOf("9") }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var macError by remember { mutableStateOf<String?>(null) }
    var broadcastError by remember { mutableStateOf<String?>(null) }
    var deviceIpError by remember { mutableStateOf<String?>(null) }
    var portError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Prendida",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Encender PC por Wake on LAN",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                nombreError = null
            },
            label = { Text("Nombre del dispositivo") },
            isError = nombreError != null,
            supportingText = {
                nombreError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = macAddress,
            onValueChange = {
                macAddress = it
                macError = null
            },
            label = { Text("Dirección MAC") },
            placeholder = { Text("Ej: 74-56-3C-54-45-D7") },
            isError = macError != null,
            supportingText = {
                macError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = broadcastIp,
            onValueChange = {
                broadcastIp = it
                broadcastError = null
            },
            label = { Text("Dirección Broadcast") },
            placeholder = { Text("Ej: 192.168.1.255") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = broadcastError != null,
            supportingText = {
                broadcastError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = deviceIp,
            onValueChange = {
                deviceIp = it
                deviceIpError = null
            },
            label = { Text("IP del dispositivo") },
            placeholder = { Text("Ej: 192.168.1.54") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = deviceIpError != null,
            supportingText = {
                deviceIpError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = portText,
            onValueChange = {
                portText = it.filter { char -> char.isDigit() }
                portError = null
            },
            label = { Text("Puerto") },
            placeholder = { Text("Ej: 9 o 7") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = portError != null,
            supportingText = {
                portError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                nombreError = validateNombre(nombre)
                macError = validateMac(macAddress)
                broadcastError = validateIp(broadcastIp, "broadcast")
                deviceIpError = validateIp(deviceIp, "IP del dispositivo")
                portError = validatePort(portText)

                val hasErrors = listOf(
                    nombreError,
                    macError,
                    broadcastError,
                    deviceIpError,
                    portError
                ).any { it != null }

                if (hasErrors) {
                    Toast.makeText(
                        context,
                        "Revisá los campos marcados",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if (!NetworkUtils.isConnectedToWifi(context)) {
                    Toast.makeText(
                        context,
                        "Tenés que estar conectado a una red Wi-Fi",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                val port = portText.toInt()

                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        WakeOnLanSender.send(
                            macAddress = macAddress,
                            broadcastIp = broadcastIp,
                            deviceIp = deviceIp,
                            port = port
                        )
                    }

                    if (result.isSuccess) {
                        Toast.makeText(
                            context,
                            "result.getOrNull() ?: \"Paquete enviado a \$nombre\"",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val error = result.exceptionOrNull()

                        Toast.makeText(
                            context,
                            "Error: ${error?.javaClass?.simpleName}: ${error?.message ?: "Sin detalle"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        ) {
            Text("Encender dispositivo")
        }
    }
}

fun validateNombre(nombre: String): String? {
    return when {
        nombre.isBlank() -> "El nombre no puede estar vacío."
        nombre.length < 2 -> "El nombre es demasiado corto."
        else -> null
    }
}

fun validateMac(mac: String): String? {
    val cleanMac = mac
        .replace("-", "")
        .replace(":", "")
        .replace(" ", "")
        .uppercase()

    return when {
        mac.isBlank() -> "La MAC no puede estar vacía."
        cleanMac.length != 12 -> "La MAC debe tener 12 caracteres hexadecimales."
        !cleanMac.all { it in '0'..'9' || it in 'A'..'F' } -> "La MAC contiene caracteres inválidos."
        else -> null
    }
}

fun validateIp(ip: String, fieldName: String): String? {
    if (ip.isBlank()) {
        return "La $fieldName no puede estar vacía."
    }

    val parts = ip.split(".")

    if (parts.size != 4) {
        return "La $fieldName no tiene formato válido."
    }

    val numbers = parts.mapNotNull { it.toIntOrNull() }

    if (numbers.size != 4) {
        return "La $fieldName solo puede contener números y puntos."
    }

    val validRange = numbers.all { it in 0..255 }

    if (!validRange) {
        return "Cada número de la $fieldName debe estar entre 0 y 255."
    }

    return null
}

fun validatePort(portText: String): String? {
    if (portText.isBlank()) {
        return "El puerto no puede estar vacío."
    }

    val port = portText.toIntOrNull()
        ?: return "El puerto debe ser un número."

    return when {
        port !in 1..65535 -> "El puerto debe estar entre 1 y 65535."
        else -> null
    }
}