package com.maximosantino.prendida.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximosantino.prendida.data.PcDeviceEntity
import com.maximosantino.prendida.ui.theme.SurfaceWhite
import com.maximosantino.prendida.ui.theme.TextGreyLight
import com.maximosantino.prendida.ui.theme.TextGreyDark
import com.maximosantino.prendida.ui.theme.TextBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    devices: List<PcDeviceEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddDeviceClick: () -> Unit,
    onEditDeviceClick: (PcDeviceEntity) -> Unit,
    onHelpClick: () -> Unit,
    onPowerClick: (PcDeviceEntity) -> Unit,
    onDeleteClick: (PcDeviceEntity) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SectionTitle(
            title = "Mis Equipos",
            subtitle = "Encendé tus PCs de forma remota"
        )

        if (devices.isEmpty() && searchQuery.isEmpty()) {
            EmptyState(
                message = "No tenés PCs guardadas",
                description = "Agregá tu primera PC para empezar a usar Prendida.",
                buttonText = "Agregar mi primera PC",
                onButtonClick = onAddDeviceClick
            )
        } else {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Buscar por nombre...", color = Color.Black.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black.copy(alpha = 0.6f)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (devices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron resultados", color = TextGreyLight)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(devices) { device ->
                        DeviceCard(
                            device = device,
                            onPowerClick = { onPowerClick(device) },
                            onEditClick = { onEditDeviceClick(device) },
                            onDeleteClick = { onDeleteClick(device) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: PcDeviceEntity,
    onPowerClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar equipo?", color = Color.White) },
            text = { Text("¿Estás seguro de que querés eliminar '${device.name}'?", color = Color.White.copy(alpha = 0.7f)) },
            containerColor = Color(0xFF1A1A1A),
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDeleteDialog = false
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextBlack
                    )
                }
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    DeviceDetailItem(icon = Icons.Default.Info, label = "MAC", value = device.macAddress)
                    DeviceDetailItem(icon = Icons.Default.Place, label = "IP", value = device.deviceIp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    DeviceDetailItem(icon = Icons.Default.Share, label = "Broadcast", value = device.broadcastIp)
                    DeviceDetailItem(icon = Icons.Default.Build, label = "Puerto", value = device.port.toString())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPowerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ENCENDER", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun DeviceDetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = TextGreyDark
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = TextGreyDark
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = TextBlack
        )
    }
}
