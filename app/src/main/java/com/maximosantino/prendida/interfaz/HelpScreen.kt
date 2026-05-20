package com.maximosantino.prendida.interfaz

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximosantino.prendida.R
import com.maximosantino.prendida.ui.theme.SurfaceWhite
import com.maximosantino.prendida.ui.theme.TextGreyDark
import com.maximosantino.prendida.ui.theme.TextBlack

@Composable
fun HelpScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        SectionTitle(
            title = "Guía de Ayuda",
            subtitle = "Seguí estos pasos para configurar tu PC"
        )

        HelpSection(
            title = "1. ¿Qué es Prendida?",
            icon = Icons.Default.Info
        ) {
            Text(
                "Prendida te permite encender tu computadora desde el celular. Para que funcione, tu PC debe estar conectada por cable y configurada para 'escuchar' el aviso de encendido  mediante la tecnologia wake on lan.",
                color = TextGreyDark
            )
        }

        HelpStep(
            stepNumber = 1,
            title = "Conectá tu PC por cable",
            description = "Asegurate de que tu computadora esté conectada al router con un cable de red (Ethernet). El Wi-Fi en la PC no suele funcionar para encenderla de forma remota. ",
            imageResList = listOf(R.drawable.paso1)
        )

        HelpStep(
            stepNumber = 2,
            title = "Buscá los datos de tu red",
            description = "En tu PC, buscá 'Símbolo del sistema', escribí 'ipconfig /all' y presioná Enter. Anotá la 'Dirección física' (MAC) y la 'Dirección IPv4'.",
            imageResList = listOf(
                R.drawable.paso2,
                R.drawable.paso3
            )


        )

        HelpStep(
            stepNumber = 3,
            title = "Configurá la tarjeta de red",
            description = "Entrá al Administrador de Dispositivos en Windows, buscá tu placa de red y en 'Propiedades' -> 'Opciones Avanzadas', activá todo lo que diga 'Wake on Magic Packet'.",
            imageResList = listOf(
                R.drawable.paso4,
                R.drawable.paso5
            )
        )

        HelpStep(
            stepNumber = 4,
            title = "Activá el encendido en la BIOS",
            description = "Reiniciá tu PC y entrá a la BIOS (presionando F2 o Supr). Buscá en 'Power Management' una opción llamada 'Wake on LAN' o 'Power On By PCI-E' y ponela en 'Enabled'.",
            imageResList = listOf(R.drawable.paso7)
        )

        HelpStep(
            stepNumber = 5,
            title = "Desactivá el inicio rápido",
            description = "En Windows, ve a opciones de Energía y desactivá el 'Inicio rápido'. Esto permite que la placa de red se quede 'esperando' el aviso cuando apagues la PC.",
            imageResList = listOf(
                R.drawable.paso8,
                R.drawable.paso105,
                R.drawable.paso12

            )
        )

        HelpStep(
            stepNumber = 6,
            title = "¡Listo para probar!",
            description = "Agregá tu PC en la aplicación usando los datos que anotaste. Asegurate de que tu celular esté conectado al mismo Wi-Fi que la PC.",
            imageResList = listOf(R.drawable.paso1)
        )

        HelpSection(
            title = "¿Necesitás más ayuda?",
            icon = Icons.Default.QuestionMark
        ) {
            BulletPoint("La dirección de Broadcast suele terminar en .255 (ej: 192.168.1.255) o sea es tu ip y 255 en caso de que no funcine preguntale a gpt que el sabe.")
            BulletPoint("Si la luz del puerto de red de tu PC se apaga por completo al apagar la PC, revisá nuevamente la BIOS.")
            BulletPoint("Puede ser que tu bios apague la luz del puerto ethernet  es raro pero pasa ")
            BulletPoint("Si la luz del puerto de red de tu PC se apaga por completo al apagar la PC, revisá nuevamente la BIOS.")
            BulletPoint("Si la luz del puerto de red de tu PC se apaga por completo al apagar la PC, revisá nuevamente la BIOS.")
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun HelpStep(
    stepNumber: Int,
    title: String,
    description: String,
    imageResList: List<Int>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stepNumber.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                color = TextGreyDark,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            imageResList.forEachIndexed { index, imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Imagen ${index + 1} del paso $stepNumber",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun HelpSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("•", color = Color.Black, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextGreyDark, style = MaterialTheme.typography.bodyMedium)
    }
}
