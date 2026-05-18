package com.maximosantino.prendida.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen() {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Cómo usar Prendida",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Prendida sirve para encender una computadora usando Wake on LAN. Para que funcione, la PC debe estar conectada por cable Ethernet y tener Wake on LAN activado en la BIOS y en Windows."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Qué es una dirección MAC?",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "La dirección MAC es un identificador único de la placa de red. Es necesaria porque el paquete Wake on LAN se envía específicamente a esa placa."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cómo sacar la MAC en Windows",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = """
1. Abrí CMD o PowerShell.
2. Escribí: ipconfig /all
3. Buscá el adaptador Ethernet.
4. Copiá el valor de "Dirección física".

Ejemplo:
74-56-3C-54-45-D7
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Qué es la IP del dispositivo?",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Es la IP local de la PC cuando está prendida. Sirve para hacer ping y verificar si responde. Ejemplo: 192.168.1.54."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Qué es la dirección broadcast?",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Es una dirección que permite mandar el paquete a todos los dispositivos de la red local. Si tu PC tiene IP 192.168.1.54, normalmente el broadcast es 192.168.1.255."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Puerto recomendado",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Usá el puerto 9. Si no funciona, probá con el 7."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Importante",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = """
- El celular debe estar conectado a la misma red Wi-Fi que la PC.
- La PC debe estar conectada por cable Ethernet.
- Wake on LAN debe estar activado en BIOS.
- ErP debe estar desactivado.
- En Windows, "Reactivar con Magic Packet" debe estar activado.
- Si la luz del puerto Ethernet queda apagada al apagar la PC, Wake on LAN no va a funcionar.
            """.trimIndent()
        )
    }
}