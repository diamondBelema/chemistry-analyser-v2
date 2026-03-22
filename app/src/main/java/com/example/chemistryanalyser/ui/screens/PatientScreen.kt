package com.example.chemistryanalyser.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chemistryanalyser.ui.TestViewModel
import com.example.chemistryanalyser.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientScreen(viewModel: TestViewModel) {
    val patients by viewModel.allPatients.collectAsState(initial = emptyList())
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Patient") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Patients Registry",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (patients.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Person,
                    message = "No patients registered yet. Add a patient to start saving analysis results.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(patients) { patient ->
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                        ) {
                            ListItem(
                                headlineContent = { Text(patient.name, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text("Age: ${patient.age} • ${patient.gender}") },
                                leadingContent = {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                patient.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }

                        Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState
        ) {
            AddPatientSheet(
                onDismiss = { showAddSheet = false },
                onConfirm = { name, age, gender ->
                    viewModel.addPatient(name, age, gender)
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
fun AddPatientSheet(onDismiss: () -> Unit, onConfirm: (String, Int, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Register New Patient", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
        Spacer(Modifier.height(24.dp))
        
        Text("Gender", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = gender == "Male",
                onClick = { gender = "Male" },
                label = { Text("Male") },
                modifier = Modifier.padding(end = 8.dp)
            )
            FilterChip(
                selected = gender == "Female",
                onClick = { gender = "Female" },
                label = { Text("Female") }
            )
        }
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { onConfirm(name, age.toIntOrNull() ?: 0, gender) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Register Patient")
        }
    }
}
