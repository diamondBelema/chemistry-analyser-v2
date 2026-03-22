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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chemistryanalyser.data.model.Test
import com.example.chemistryanalyser.ui.TestViewModel
import com.example.chemistryanalyser.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(viewModel: TestViewModel) {
    val tests by viewModel.allTests.collectAsState(initial = emptyList())
    var showEditSheet by remember { mutableStateOf<Test?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val categories = tests.groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Configurations", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Test") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (tests.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Science,
                    message = "No tests configured yet.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    categories.forEach { (category, testsInCategory) ->
                        item {
                            CategoryHeader(category)
                        }
                        items(testsInCategory) { test ->
                            TestExpressiveCard(
                                test = test,
                                onEdit = { showEditSheet = test }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }, sheetState = sheetState) {
            TestEditSheet(
                title = "Create New Test",
                onDismiss = { showAddSheet = false },
                onConfirm = { viewModel.addTest(it); showAddSheet = false }
            )
        }
    }

    if (showEditSheet != null) {
        ModalBottomSheet(onDismissRequest = { showEditSheet = null }, sheetState = sheetState) {
            TestEditSheet(
                title = "Edit Configuration",
                initialTest = showEditSheet,
                onDismiss = { showEditSheet = null },
                onConfirm = { viewModel.addTest(it); showEditSheet = null }
            )
        }
    }
}

@Composable
fun CategoryHeader(name: String) {
    Text(
        text = name.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
    )
}

@Composable
fun TestExpressiveCard(test: Test, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        ListItem(
            headlineContent = { Text(test.name, fontWeight = FontWeight.Bold) },
            supportingContent = {
                Column {
                    Text("Standard: ${test.standardConcentration} @ ${test.standardAbsorbance} abs")
                    Text("Normal Range: ${test.minNormal} - ${test.maxNormal} ${test.unit}", color = MaterialTheme.colorScheme.primary)
                }
            },
            leadingContent = {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${test.optimalWavelength}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            trailingContent = {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TestEditSheet(
    title: String,
    initialTest: Test? = null,
    onDismiss: () -> Unit,
    onConfirm: (Test) -> Unit
) {
    var name by remember { mutableStateOf(initialTest?.name ?: "") }
    var category by remember { mutableStateOf(initialTest?.category ?: "General") }
    var conc by remember { mutableStateOf(initialTest?.standardConcentration?.toString() ?: "") }
    var abs by remember { mutableStateOf(initialTest?.standardAbsorbance?.toString() ?: "") }
    var wave by remember { mutableStateOf(initialTest?.optimalWavelength?.toString() ?: "") }
    var minN by remember { mutableStateOf(initialTest?.minNormal?.toString() ?: "") }
    var maxN by remember { mutableStateOf(initialTest?.maxNormal?.toString() ?: "") }
    var unit by remember { mutableStateOf(initialTest?.unit ?: "mg/dL") }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        
        item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Test Name") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) }
        
        item { 
            val categories = listOf("Lipid Profile", "Liver Function", "General", "Kidney Function")
            Text("Category", style = MaterialTheme.typography.labelLarge)
            FlowRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }
        
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = conc, onValueChange = { conc = it }, label = { Text("Std. Conc") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
                OutlinedTextField(value = abs, onValueChange = { abs = it }, label = { Text("Std. Abs") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = minN, onValueChange = { minN = it }, label = { Text("Min Normal") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
                OutlinedTextField(value = maxN, onValueChange = { maxN = it }, label = { Text("Max Normal") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
            }
        }
        
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = wave, onValueChange = { wave = it }, label = { Text("Wavelength") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
            }
        }
        
        item {
            Button(
                onClick = {
                    val test = Test(
                        id = initialTest?.id ?: name.lowercase().replace(" ", "_"),
                        name = name,
                        standardConcentration = conc.toDoubleOrNull() ?: 0.0,
                        standardAbsorbance = abs.toDoubleOrNull() ?: 1.0,
                        optimalWavelength = wave.toIntOrNull() ?: 500,
                        category = category,
                        minNormal = minN.toDoubleOrNull() ?: 0.0,
                        maxNormal = maxN.toDoubleOrNull() ?: 0.0,
                        unit = unit
                    )
                    onConfirm(test)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Save Configuration")
            }
        }
    }
}
