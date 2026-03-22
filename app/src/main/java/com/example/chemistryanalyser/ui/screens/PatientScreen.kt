package com.example.chemistryanalyser.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chemistryanalyser.ui.TestViewModel

@Composable
fun PatientScreen(viewModel: TestViewModel) {
    val patients by viewModel.allPatients.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Patient")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(patients) { patient ->
                ListItem(
                    headlineContent = { Text(patient.name) },
                    supportingContent = { Text("Age: ${patient.age} | Gender: ${patient.gender}") }
                )
                HorizontalDivider()
            }
        }
    }

    if (showAddDialog) {
        AddPatientDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, age, gender ->
                viewModel.addPatient(name, age, gender)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddPatientDialog(onDismiss: () -> Unit, onConfirm: (String, Int, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Patient") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = age, onValueChange = { age = it }, label = { Text("Age") })
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Male", onClick = { gender = "Male" })
                    Text("Male")
                    RadioButton(selected = gender == "Female", onClick = { gender = "Female" })
                    Text("Female")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, age.toIntOrNull() ?: 0, gender) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
