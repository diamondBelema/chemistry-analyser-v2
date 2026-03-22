package com.example.chemistryanalyser.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chemistryanalyser.data.model.Test
import com.example.chemistryanalyser.ui.TestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(viewModel: TestViewModel) {
    val tests by viewModel.allTests.collectAsState(initial = emptyList())
    var showEditSheet by remember { mutableStateOf<Test?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Test") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Test Configurations",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (tests.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Science,
                    message = "No tests configured yet. Create one to start analyzing.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tests) { test ->
                        TestListItem(
                            test = test,
                            onEdit = { showEditSheet = test }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
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
            TestEditSheet(
                title = "Create New Test",
                onDismiss = { showAddSheet = false },
                onConfirm = { newTest ->
                    viewModel.addTest(newTest)
                    showAddSheet = false
                }
            )
        }
    }

    if (showEditSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = null },
            sheetState = sheetState
        ) {
            TestEditSheet(
                title = "Edit Configuration",
                initialTest = showEditSheet,
                onDismiss = { showEditSheet = null },
                onConfirm = { updatedTest ->
                    viewModel.addTest(updatedTest)
                    showEditSheet = null
                }
            )
        }
    }
}

@Composable
fun TestListItem(test: Test, onEdit: () -> Unit) {
    ListItem(
        headlineContent = { 
            Text(test.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) 
        },
        supportingContent = { 
            Column {
                Text(
                    "Standard: ${test.standardConcentration} conc @ ${test.standardAbsorbance} abs",
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        "${test.optimalWavelength} nm",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        },
        leadingContent = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Configuration")
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun TestEditSheet(
    title: String,
    initialTest: Test? = null,
    onDismiss: () -> Unit,
    onConfirm: (Test) -> Unit
) {
    var name by remember { mutableStateOf(initialTest?.name ?: "") }
    var conc by remember { mutableStateOf(initialTest?.standardConcentration?.toString() ?: "") }
    var abs by remember { mutableStateOf(initialTest?.standardAbsorbance?.toString() ?: "") }
    var wave by remember { mutableStateOf(initialTest?.optimalWavelength?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Test Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
        Spacer(Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = conc,
                onValueChange = { conc = it },
                label = { Text("Std. Conc") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            )
            OutlinedTextField(
                value = abs,
                onValueChange = { abs = it },
                label = { Text("Std. Abs") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            )
        }
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = wave,
            onValueChange = { wave = it },
            label = { Text("Optimal Wavelength (nm)") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = {
                val test = Test(
                    id = initialTest?.id ?: name.lowercase().replace(" ", "_"),
                    name = name,
                    standardConcentration = conc.toDoubleOrNull() ?: 0.0,
                    standardAbsorbance = abs.toDoubleOrNull() ?: 1.0,
                    optimalWavelength = wave.toIntOrNull() ?: 500,
                    category = initialTest?.category ?: "Custom"
                )
                onConfirm(test)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(if (initialTest == null) "Create Configuration" else "Save Changes")
        }
    }
}
