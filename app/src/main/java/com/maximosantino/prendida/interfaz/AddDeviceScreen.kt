package com.maximosantino.prendida.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.maximosantino.prendida.data.PcDeviceEntity
import com.maximosantino.prendida.network.NetworkUtils
import com.maximosantino.prendida.ui.theme.TextGreyLight

@Composable
fun AddDeviceScreen(
    modifier: Modifier = Modifier,
    deviceToEdit: PcDeviceEntity? = null,
    onSaveDevice: (String, String, String, String, Int) -> Unit,
    onUpdateDevice: (Int, String, String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(deviceToEdit?.name ?: "") }
    var macAddress by remember { mutableStateOf(deviceToEdit?.macAddress ?: "") }
    var deviceIp by remember { mutableStateOf(deviceToEdit?.deviceIp ?: "") }
    var subnetMask by remember { 
        mutableStateOf(
            if (deviceToEdit != null) 
                NetworkUtils.inferSubnetMask(deviceToEdit.deviceIp, deviceToEdit.broadcastIp) 
            else "255.255.255.0"
        ) 
    }
    var port by remember { mutableStateOf(deviceToEdit?.port?.toString() ?: "9") }

    // Estados de error
    var nameError by remember { mutableStateOf<String?>(null) }
    var macError by remember { mutableStateOf<String?>(null) }
    var ipError by remember { mutableStateOf<String?>(null) }
    var maskError by remember { mutableStateOf<String?>(null) }
    var portError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    fun validateForm(): Boolean {
        var isValid = true

        // Validar Nombre
        if (name.isBlank()) {
            nameError = "El nombre no puede estar vacío"
            isValid = false
        } else if (name.length < 2) {
            nameError = "El nombre debe tener al menos 2 caracteres"
            isValid = false
        } else {
            nameError = null
        }

        // Validar MAC
        val cleanMac = macAddress.replace(":", "").replace("-", "").trim()
        val macRegex = "^[0-9A-Fa-f]{12}$".toRegex()
        if (cleanMac.isBlank()) {
            macError = "La MAC no puede estar vacía"
            isValid = false
        } else if (!macRegex.matches(cleanMac)) {
            macError = "Formato inválido. Debe tener 12 caracteres hexadecimales."
            isValid = false
        } else {
            macError = null
        }

        // Validar IP
        val ipRegex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$".toRegex()
        if (deviceIp.isBlank()) {
            ipError = "La IP no puede estar vacía"
            isValid = false
        } else if (!ipRegex.matches(deviceIp)) {
            ipError = "IP inválida. Ejemplo: 192.168.1.54"
            isValid = false
        } else {
            ipError = null
        }

        // Validar Máscara
        if (subnetMask.isBlank()) {
            maskError = "La máscara no puede estar vacía"
            isValid = false
        } else if (!ipRegex.matches(subnetMask)) {
            maskError = "Máscara inválida. Ejemplo: 255.255.255.0"
            isValid = false
        } else {
            maskError = null
        }

        // Validar Puerto
        val portInt = port.toIntOrNull()
        if (port.isBlank()) {
            portError = "El puerto no puede estar vacío"
            isValid = false
        } else if (portInt == null || portInt !in 1..65535) {
            portError = "El puerto debe estar entre 1 y 65535"
            isValid = false
        } else {
            portError = null
        }

        return isValid
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        SectionTitle(
            title = if (deviceToEdit == null) "Nuevo Equipo" else "Editar Equipo",
            subtitle = if (deviceToEdit == null) "Completá los datos técnicos de tu PC" else "Modificá los datos de '${deviceToEdit.name}'"
        )

        PrendidaTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre de la PC",
            placeholder = "Ej: PC Gamer / Oficina",
            helperText = "¿Cómo querés llamar a este equipo?",
            isError = nameError != null,
            errorMessage = nameError,
            leadingIcon = Icons.Default.Edit
        )

        PrendidaTextField(
            value = macAddress,
            onValueChange = { macAddress = it },
            label = "Dirección MAC",
            placeholder = "Ej: 74:56:3C:54:45:D7",
            helperText = "Identificador único de tu placa de red",
            isError = macError != null,
            errorMessage = macError,
            leadingIcon = Icons.Default.Settings
        )

        PrendidaTextField(
            value = deviceIp,
            onValueChange = { deviceIp = it },
            label = "Dirección IP",
            placeholder = "Ej: 192.168.1.54",
            helperText = "Dirección local de la PC en tu red",
            isError = ipError != null,
            errorMessage = ipError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            leadingIcon = Icons.Default.Place
        )

        PrendidaTextField(
            value = subnetMask,
            onValueChange = { subnetMask = it },
            label = "Máscara de Red",
            placeholder = "Ej: 255.255.255.0",
            helperText = "Usada para calcular el broadcast automáticamente",
            isError = maskError != null,
            errorMessage = maskError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            leadingIcon = Icons.Default.Share
        )

        PrendidaTextField(
            value = port,
            onValueChange = { port = it },
            label = "Puerto",
            placeholder = "Por defecto 9",
            helperText = "Puerto para Wake on LAN (usualmente 9)",
            isError = portError != null,
            errorMessage = portError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = Icons.Default.Build
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = if (deviceToEdit == null) "GUARDAR EQUIPO" else "ACTUALIZAR DATOS",
            onClick = {
                if (validateForm()) {
                    val calculatedBroadcast = NetworkUtils.calculateBroadcastAddress(deviceIp, subnetMask) ?: "255.255.255.255"
                    
                    if (deviceToEdit == null) {
                        onSaveDevice(
                            name,
                            macAddress.uppercase().trim(),
                            calculatedBroadcast,
                            deviceIp.trim(),
                            port.toInt()
                        )
                    } else {
                        onUpdateDevice(
                            deviceToEdit.id,
                            name,
                            macAddress.uppercase().trim(),
                            calculatedBroadcast,
                            deviceIp.trim(),
                            port.toInt()
                        )
                    }
                }
            },
            icon = if (deviceToEdit == null) Icons.Default.Check else Icons.Default.Refresh
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
