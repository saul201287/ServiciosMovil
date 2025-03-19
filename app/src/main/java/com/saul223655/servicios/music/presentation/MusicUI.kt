package com.saul223655.servicios.music.presentation

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saul223655.servicios.core.navigation.Screen
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    val isPlaying by viewModel.isPlaying.observeAsState(false)
    val history by viewModel.history.observeAsState(emptyList())
    val availableSongs by viewModel.availableSongs.observeAsState(emptyList())

    var showHistoryModal by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(
                    text = "Menú",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Cargar canciones") },
                    label = { Text("Cargar canciones") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.StoragePermission.route)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Ver Historial") },
                    selected = false,
                    onClick = {
                        viewModel.loadHistory()
                        showHistoryModal = true
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Reproductor de Música") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPlaying) "Reproduciendo" else "En pausa",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = {
                                if (availableSongs.isEmpty()) {
                                    viewModel.playDefaultAudio()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproducir",
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        IconButton(
                            onClick = { if (isPlaying) viewModel.pauseMusic() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pausar",
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        IconButton(
                            onClick = { if (isPlaying) viewModel.stopMusic() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Detener",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .heightIn(max = 200.dp)
                    ) {
                        items(availableSongs) { song ->
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.playMusic(song) }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showHistoryModal) {
        ModalBottomSheet(
            onDismissRequest = { showHistoryModal = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Historial de Canciones",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(history) { song ->
                        val date = SimpleDateFormat(
                            "dd/MM/yyyy HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date(song.timestamp))
                        Text(
                            text = "${song.title} - $date",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { viewModel.deleteHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Borrar Historial")
                    }
                    Button(
                        onClick = { showHistoryModal = false }
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}
