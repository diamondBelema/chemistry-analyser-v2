package com.example.chemistryanalyser.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chemistryanalyser.data.model.Test
import com.example.chemistryanalyser.ui.TestViewModel

@Composable
fun TestsScreen(viewModel: TestViewModel) {
    val tests by viewModel.allTests.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTest by remember { mutableStateOf<Test?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Test")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(tests) { test ->
                ListItem(
                    headlineContent = { Text(test.name) },
                    supportingContent = { 
                        Text("Std Conc: ${test.standardConcentration} | Std Abs: ${test.standardAbsorbance} | Wavelength: ${test.optimalWavelength}nm") 
                    },
                    trailingContent = {
                        IconButton(onClick = { editingTest = test }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showAddDialog) {
        TestEditDialog(
            title = "Add New Test",
            onDismiss = { showAddDialog = false },
            onConfirm = { newTest ->
                viewModel.addTest(newTest)
                showAddDialog = false
            }
        )
    }

    if (editingTest != null) {
        TestEditDialog(
            title = "Edit Test",
            initialTest = editingTest,
            onDismiss = { editingTest = null },
            onConfirm = { updatedTest ->
                viewModel.addTest(updatedTest) // Room @Insert(REPLACE) works for update
                editingTest = null
            }
        )
    }
}

@Composable
fun TestEditDialog(
    title: String,
    initialTest: Test? = null,
    onDismiss: () -> Unit,
    onConfirm: (Test) -> Unit
) {
    var name by remember { mutableStateOf(initialTest?.name ?: "") }
    var conc by remember { mutableStateOf(initialTest?.standardConcentration?.toString() ?: "") }
    var abs by remember { mutableStateOf(initialTest?.standardAbsorbance?.toString() ?: "") }
    var wave by remember { mutableStateOf(initialTest?.optimalWavelength?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Test Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = conc,
                    onValueChange = { conc = it },
                    label = { Text("Standard Concentration") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = abs,
                    onValueChange = { abs = it },
                    label = { Text("Standard Absorbance") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = wave,
                    onValueChange = { wave = it },
                    label = { Text("Optimal Wavelength (nm)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val test = Test(
                    id = initialTest?.id ?: name.lowercase().replace(" ", "_"),
                    name = name,
                    standardConcentration = conc.toDoubleOrNull() ?: 0.0,
                    standardAbsorbance = abs.toDoubleOrNull() ?: 1.0,
                    optimalWavelength = wave.toIntOrNull() ?: 500,
                    category = initialTest?.category ?: "Custom"
                )
                onConfirm(test)
            }) {
                Text(if (initialTest == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
