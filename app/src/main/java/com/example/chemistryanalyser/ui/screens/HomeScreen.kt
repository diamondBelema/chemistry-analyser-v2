package com.example.chemistryanalyser.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chemistryanalyser.data.model.Patient
import com.example.chemistryanalyser.data.model.Test
import com.example.chemistryanalyser.ui.ConnectionState
import com.example.chemistryanalyser.ui.TestViewModel

@Composable
fun HomeScreen(viewModel: TestViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isCalculating by viewModel.isCalculating.collectAsState()
    
    val tests by viewModel.allTests.collectAsState(initial = emptyList())
    val patients by viewModel.allPatients.collectAsState(initial = emptyList())

    var selectedTest by remember { mutableStateOf<Test?>(null) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var showPatientDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Connection Header
        ConnectionCard(
            connectionState = connectionState,
            statusMessage = statusMessage,
            onConnect = { viewModel.connect() }
        )

        Spacer(Modifier.height(24.dp))

        // Patient Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showPatientDialog = true }
        ) {
            ListItem(
                headlineContent = { Text(selectedPatient?.name ?: "Select Patient") },
                supportingContent = { Text(selectedPatient?.let { "Age: ${it.age}, Gender: ${it.gender}" } ?: "Required to save results") },
                leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Test Selection
        Text(
            "Select Analysis",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tests) { test ->
                FilterChip(
                    selected = selectedTest == test,
                    onClick = { selectedTest = test },
                    label = { Text(test.name) },
                    modifier = Modifier.padding(4.dp),
                    leadingIcon = if (selectedTest == test) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }

        // Action Buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isCalculating) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                }
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { viewModel.setBlank() },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        enabled = connectionState == ConnectionState.CONNECTED && !isCalculating
                    ) {
                        Text("Set Blank")
                    }
                    
                    Button(
                        onClick = { 
                            selectedTest?.let { viewModel.performTest(it, selectedPatient) }
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        enabled = connectionState == ConnectionState.CONNECTED && 
                                 selectedTest != null && !isCalculating
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Run Test")
                    }
                }
            }
        }
    }

    if (showPatientDialog) {
        PatientSelectionDialog(
            patients = patients,
            onSelect = { selectedPatient = it; showPatientDialog = false },
            onDismiss = { showPatientDialog = false }
        )
    }
}

@Composable
fun ConnectionCard(
    connectionState: ConnectionState,
    statusMessage: String,
    onConnect: () -> Unit
) {
    val containerColor = when (connectionState) {
        ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
        ConnectionState.ERROR -> MaterialTheme.colorScheme.errorContainer
        ConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val icon = when (connectionState) {
        ConnectionState.CONNECTED -> Icons.Default.Wifi
        ConnectionState.ERROR -> Icons.Default.WifiOff
        ConnectionState.CONNECTING -> Icons.Default.Sync
        else -> Icons.Default.CloudOff
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when(connectionState) {
                        ConnectionState.CONNECTED -> "Connected"
                        ConnectionState.CONNECTING -> "Connecting..."
                        ConnectionState.ERROR -> "Connection Failed"
                        else -> "Disconnected"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = statusMessage, style = MaterialTheme.typography.bodySmall)
            }
            
            if (connectionState != ConnectionState.CONNECTED && connectionState != ConnectionState.CONNECTING) {
                Button(onClick = onConnect) {
                    Text("Connect")
                }
            } else if (connectionState == ConnectionState.CONNECTED) {
                IconButton(onClick = onConnect) {
                    Icon(Icons.Default.Refresh, contentDescription = "Check Status")
                }
            }
        }
    }
}

@Composable
fun PatientSelectionDialog(
    patients: List<Patient>,
    onSelect: (Patient) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Patient") },
        text = {
            if (patients.isEmpty()) {
                Text("No patients found. Please add a patient in the Patients tab.")
            } else {
                LazyColumn {
                    items(patients) { patient ->
                        ListItem(
                            headlineContent = { Text(patient.name) },
                            supportingContent = { Text("Age: ${patient.age}") },
                            modifier = Modifier.clickable { onSelect(patient) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
