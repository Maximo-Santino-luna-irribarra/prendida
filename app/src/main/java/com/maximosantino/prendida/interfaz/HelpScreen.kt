package com.maximosantino.prendida.interfaz

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximosantino.prendida.ui.theme.SurfaceWhite
import com.maximosantino.prendida.ui.theme.TextGreyLight
import com.maximosantino.prendida.ui.theme.TextGreyDark
import com.maximosantino.prendida.ui.theme.TextBlack
import androidx.compose.ui.graphics.Color
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
            subtitle = "Todo lo que necesitas saber para empezar"
        )

        HelpSection(
            title = "1. ¿Qué es Prendida?",
            icon = Icons.Default.Info
        ) {
            Text(
                "Prendida permite encender tu computadora de escritorio desde el celular usando una tecnología llamada 'Wake on LAN'.",
                color = TextGreyDark
            )
        }

        HelpSection(
            title = "2. Requisitos Previos",
            icon = Icons.Default.List
        ) {
            BulletPoint("Tu PC debe estar conectada por CABLE de red (Ethernet).")
            BulletPoint("Tu celular debe estar en la misma red Wi-Fi que la PC.")
            BulletPoint("La PC debe tener habilitado Wake on LAN en la BIOS y Windows.")
        }

        HelpSection(
            title = "3. Cómo encontrar la MAC e IP",
            icon = Icons.Default.Search
        ) {
            Text(
                "En tu PC, abrí el 'Símbolo del sistema' (buscá 'cmd') y escribí:",
                color = TextGreyDark
            )
            Surface(
                color = Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    "ipconfig /all",
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Text(
                "Buscá el apartado 'Adaptador de Ethernet'.\n- Dirección física: es la MAC.\n- Dirección IPv4: es la IP.",
                color = TextGreyDark
            )
            ImagePlaceholder(text = "Imagen pendiente: ejemplo de ipconfig /all")
        }

        HelpSection(
            title = "4. Dirección de Broadcast",
            icon = Icons.Default.Share
        ) {
            Text(
                "Normalmente es tu dirección IP pero terminada en .255. Por ejemplo, si tu IP es 192.168.1.54, el broadcast suele ser 192.168.1.255.",
                color = TextGreyDark
            )
        }

        HelpSection(
            title = "5. Configuración en Windows",
            icon = Icons.Default.Settings
        ) {
            Text(
                "Debes ir al Administrador de Dispositivos, buscar tu tarjeta de red, entrar en Propiedades -> Opciones Avanzadas y activar 'Wake on Magic Packet'.",
                color = TextGreyDark
            )
            ImagePlaceholder(text = "Imagen pendiente: configuración Wake on LAN en Windows")
        }

        HelpSection(
            title = "6. Configuración en BIOS",
            icon = Icons.Default.Build
        ) {
            Text(
                "Al prender la PC (antes de Windows), entra a la BIOS (tecla F2, F12 o Supr) y busca opciones como 'Wake on LAN', 'Power On By PCI-E' o 'Remote Wake Up'.",
                color = TextGreyDark
            )
            ImagePlaceholder(text = "Imagen pendiente: configuración BIOS")
        }

        HelpSection(
            title = "7. Si no funciona...",
            icon = Icons.Default.Warning
        ) {
            BulletPoint("Verificá que la luz del puerto Ethernet de tu PC quede prendida aunque la PC esté apagada.")
            ImagePlaceholder(text = "Imagen pendiente: luz del puerto Ethernet")
            BulletPoint("Desactivá el 'Inicio rápido' en Windows.")
            BulletPoint("Asegurate de que no haya un firewall bloqueando el puerto 9.")
        }

        Spacer(modifier = Modifier.height(40.dp))
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
