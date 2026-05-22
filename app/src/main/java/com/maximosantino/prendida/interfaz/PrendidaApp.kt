package com.maximosantino.prendida.interfaz

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maximosantino.prendida.R
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Cambiar color de iconos de la barra de estado según si el menú está abierto
    val view = LocalView.current
    val isDrawerOpen = drawerState.isOpen || drawerState.isAnimationRunning
    
    SideEffect {
        val window = (view.context as android.app.Activity).window
        androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isDrawerOpen
    }

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
    val deviceStatuses by viewModel.deviceStatuses.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startService(context)
        viewModel.uiEvents.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

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

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
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
                        .statusBarsPadding()
                        .padding(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo4),
                            contentDescription = "Logo Prendida",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.width(12.dp))
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
                        if (currentScreen == PrendidaScreen.HOME) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo4),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "PRENDIDA",
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                            }
                        } else {
                            Text(
                                text = when (currentScreen) {
                                    PrendidaScreen.ADD_DEVICE -> "AGREGAR PC"
                                    PrendidaScreen.EDIT_DEVICE -> "EDITAR EQUIPO"
                                    PrendidaScreen.HELP -> "AYUDA"
                                    else -> "PRENDIDA"
                                },
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
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
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                if (currentScreen == PrendidaScreen.HOME || currentScreen == PrendidaScreen.HELP) {
                    FloatingActionButton(
                        onClick = { currentScreen = PrendidaScreen.ADD_DEVICE },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar PC")
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        containerColor = Color(0xFF333333),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        snackbarData = data
                    )
                }
            }
        ) { paddingValues ->
            when (currentScreen) {
                PrendidaScreen.HOME -> {
                    MainScreen(
                        modifier = Modifier.padding(paddingValues),
                        devices = devices,
                        deviceStatuses = deviceStatuses,
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
                        onPowerClick = { device, _ ->
                            if (!NetworkUtils.isConnectedToWifi(context)) {
                                showMessage("Tenés que estar conectado a una red Wi-Fi para usar Wake on LAN.")
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
                                    showMessage("Magic Packet enviado. Comprobando estado...")
                                    // Iniciamos el ping una vez enviado el paquete
                                    viewModel.startCheckingStatus(context, device.id, device.deviceIp, device.name)
                                } else {
                                    showMessage("Fallo el envío del Magic Packet a ${device.name}")
                                }
                            }
                        },
                        onStopPowerClick = { device ->
                            viewModel.stopCheckingStatus(device.id)
                            showMessage("Comprobación cancelada para ${device.name}")
                        },
                        onOnlyPingClick = { device ->
                            viewModel.checkDeviceOnce(context, device.id, device.deviceIp, device.name)
                        },
                        onDeleteClick = { device ->
                            viewModel.deleteDevice(device)
                            showMessage("Equipo '${device.name}' eliminado")
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

                            showMessage("PC guardada correctamente")

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

                            showMessage("PC actualizada correctamente")
                            
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
