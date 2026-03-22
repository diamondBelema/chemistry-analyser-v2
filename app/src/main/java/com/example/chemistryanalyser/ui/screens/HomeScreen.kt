package com.example.chemistryanalyser.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chemistryanalyser.data.model.Patient
import com.example.chemistryanalyser.data.model.Test
import com.example.chemistryanalyser.ui.ConnectionState
import com.example.chemistryanalyser.ui.TestViewModel
import com.example.chemistryanalyser.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TestViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isCalculating by viewModel.isCalculating.collectAsState()
    
    val tests by viewModel.allTests.collectAsState(initial = emptyList())
    val patients by viewModel.allPatients.collectAsState(initial = emptyList())

    var selectedTest by remember { mutableStateOf<Test?>(null) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var showPatientSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                title = {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant
                            .copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(2.dp),
                        ) {
                            Text(
                                text = when(connectionState) {
                                    ConnectionState.CONNECTED -> "Analyser Online"
                                    ConnectionState.CONNECTING -> "Connecting..."
                                    ConnectionState.ERROR -> "Connection Failed"
                                    else -> "Analyser Offline"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    val icon = when (connectionState) {
                        ConnectionState.CONNECTED -> Icons.Default.Wifi
                        ConnectionState.ERROR -> Icons.Default.WifiOff
                        ConnectionState.CONNECTING -> Icons.Default.Sync
                        else -> Icons.Default.CloudOff
                    }
                    Box(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
                        Icon(icon, contentDescription = null)
                    }
                },
                actions = {
                    if (connectionState != ConnectionState.CONNECTED && connectionState != ConnectionState.CONNECTING) {
                        FilledIconButton(onClick = { viewModel.connect() }) {
                            Icon(Icons.Default.Link, contentDescription = "Connect")
                        }
                    } else if (connectionState == ConnectionState.CONNECTED) {
                        FilledIconButton(onClick = { viewModel.connect() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Check Status")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = when (connectionState) {
                        ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                        ConnectionState.ERROR -> MaterialTheme.colorScheme.errorContainer
                        ConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Patient Selection - Expressive Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showPatientSheet = true },
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            selectedPatient?.name ?: "Select Patient",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    supportingContent = { 
                        Text(
                            selectedPatient?.let { "Age: ${it.age} • ${it.gender}" } ?: "Required to save results",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    leadingContent = { 
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Test Selection Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Analysis",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (tests.isNotEmpty()) {
                    Text(
                        "${tests.size} items",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (tests.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Science,
                    message = "No tests configured. Go to the Tests tab to add one.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tests) { test ->
                        AnalysisCard(
                            test = test,
                            isSelected = selectedTest == test,
                            onClick = { selectedTest = if (selectedTest == test) null else test }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isCalculating) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.setBlank() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            enabled = connectionState == ConnectionState.CONNECTED && !isCalculating,
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("Set Blank")
                        }
                        
                        Button(
                            onClick = { 
                                selectedTest?.let { viewModel.performTest(it, selectedPatient) }
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            enabled = connectionState == ConnectionState.CONNECTED && 
                                     selectedTest != null && !isCalculating,
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Run Analysis")
                        }
                    }
                }
            }
        }
    }

    if (showPatientSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPatientSheet = false },
            sheetState = sheetState
        ) {
            PatientSelectionSheet(
                patients = patients,
                onSelect = { selectedPatient = it; showPatientSheet = false }
            )
        }
    }
}

@Composable
fun AnalysisCard(
    test: Test,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick = onClick,
        modifier = Modifier.height(115.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Science,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                test.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2
            )
            Text(
                "Range: ${test.minNormal}-${test.maxNormal}",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun PatientSelectionSheet(
    patients: List<Patient>,
    onSelect: (Patient) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Select Patient",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        
        if (patients.isEmpty()) {
            Box(modifier = Modifier.height(200.dp)) {
                EmptyState(
                    icon = Icons.Default.PersonSearch,
                    message = "No patients found. Add them in the Patients tab."
                )
            }
        } else {
            LazyColumn {
                items(patients) { patient ->
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        ListItem(
                            headlineContent = { Text(patient.name, fontWeight = FontWeight.Medium) },
                            supportingContent = { Text("Age: ${patient.age} • ${patient.gender}") },
                            leadingContent = { 
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(patient.name.take(1).uppercase())
                                    }
                                }
                            },
                            modifier = Modifier.clickable { onSelect(patient) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
