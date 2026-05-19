package com.maximosantino.prendida.interfaz

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maximosantino.prendida.data.PcDeviceEntity
import com.maximosantino.prendida.data.PrendidaDatabase
import com.maximosantino.prendida.network.NetworkUtils
import com.maximosantino.prendida.viewmodel.PrendidaViewModel
import com.maximosantino.prendida.wol.WakeOnLanSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class PrendidaScreen {
    HOME,
    ADD_DEVICE,
    HELP,
    EDIT_DEVICE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrendidaApp() {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val database = remember {
        PrendidaDatabase.getDatabase(context)
    }

    val viewModel: PrendidaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PrendidaViewModel(database.pcDeviceDao()) as T
            }
        }
    )

    val devices by viewModel.filteredDevices.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState(initial = "")

    var currentScreen by remember {
        mutableStateOf(PrendidaScreen.HOME)
    }
    
    var deviceToEdit by remember { mutableStateOf<PcDeviceEntity?>(null) }

    fun navigateTo(screen: PrendidaScreen) {
        currentScreen = screen
        if (screen != PrendidaScreen.EDIT_DEVICE) {
            deviceToEdit = null
        }
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "PRENDIDA",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Wake on LAN Utility",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio", fontWeight = FontWeight.Bold) },
                    selected = currentScreen == PrendidaScreen.HOME,
                    onClick = { navigateTo(PrendidaScreen.HOME) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Agregar PC", fontWeight = FontWeight.Bold) },
                    selected = currentScreen == PrendidaScreen.ADD_DEVICE,
                    onClick = { navigateTo(PrendidaScreen.ADD_DEVICE) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Ayuda", fontWeight = FontWeight.Bold) },
                    selected = currentScreen == PrendidaScreen.HELP,
                    onClick = { navigateTo(PrendidaScreen.HELP) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                PrendidaScreen.HOME -> "PRENDIDA"
                                PrendidaScreen.ADD_DEVICE -> "AGREGAR PC"
                                PrendidaScreen.EDIT_DEVICE -> "EDITAR EQUIPO"
                                PrendidaScreen.HELP -> "AYUDA"
                            },
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            when (currentScreen) {
                PrendidaScreen.HOME -> {
                    MainScreen(
                        modifier = Modifier.padding(paddingValues),
                        devices = devices,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onAddDeviceClick = {
                            currentScreen = PrendidaScreen.ADD_DEVICE
                        },
                        onEditDeviceClick = { device ->
                            deviceToEdit = device
                            currentScreen = PrendidaScreen.EDIT_DEVICE
                        },
                        onHelpClick = {
                            currentScreen = PrendidaScreen.HELP
                        },
                        onPowerClick = { device ->
                            if (!NetworkUtils.isConnectedToWifi(context)) {
                                Toast.makeText(
                                    context,
                                    "Tenés que estar conectado a una red Wi-Fi para usar Wake on LAN.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@MainScreen
                            }

                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    WakeOnLanSender.send(
                                        macAddress = device.macAddress,
                                        broadcastIp = device.broadcastIp,
                                        deviceIp = device.deviceIp,
                                        port = device.port
                                    )
                                }

                                if (result.isSuccess) {
                                    Toast.makeText(
                                        context,
                                        "Magic Packet enviado correctamente a ${device.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Fallo el envío del Magic Packet a ${device.name}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        onDeleteClick = { device ->
                            viewModel.deleteDevice(device)
                            Toast.makeText(
                                context,
                                "Equipo '${device.name}' eliminado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                PrendidaScreen.ADD_DEVICE, PrendidaScreen.EDIT_DEVICE -> {
                    AddDeviceScreen(
                        modifier = Modifier.padding(paddingValues),
                        deviceToEdit = if (currentScreen == PrendidaScreen.EDIT_DEVICE) deviceToEdit else null,
                        onSaveDevice = { name, macAddress, broadcastIp, deviceIp, port ->
                            viewModel.addDevice(
                                name = name,
                                macAddress = macAddress,
                                broadcastIp = broadcastIp,
                                deviceIp = deviceIp,
                                port = port
                            )

                            Toast.makeText(
                                context,
                                "PC guardada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            currentScreen = PrendidaScreen.HOME
                        },
                        onUpdateDevice = { id, name, macAddress, broadcastIp, deviceIp, port ->
                            viewModel.updateDevice(
                                id = id,
                                name = name,
                                macAddress = macAddress,
                                broadcastIp = broadcastIp,
                                deviceIp = deviceIp,
                                port = port
                            )

                            Toast.makeText(
                                context,
                                "PC actualizada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            deviceToEdit = null
                            currentScreen = PrendidaScreen.HOME
                        }
                    )
                }

                PrendidaScreen.HELP -> {
                    HelpScreen(
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
