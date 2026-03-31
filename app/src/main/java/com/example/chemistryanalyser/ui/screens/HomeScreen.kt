package com.example.chemistryanalyser.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
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

    // FIX 1: Proper scroll behaviour so LargeTopAppBar collapses smoothly
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    // FIX 2: skipPartiallyExpanded avoids the half-open sheet flicker
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp > 600

    // FIX 3: HomeScreen must NOT wrap itself in a Scaffold — it already lives inside
    // MainActivity's Scaffold which owns the NavigationBar. A nested Scaffold causes
    // double inner-padding and the bottom bar area gets eaten twice.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Chemistry Analyser",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (text, color) = when (connectionState) {
                                ConnectionState.CONNECTED   -> "Online"     to MaterialTheme.colorScheme.primary
                                ConnectionState.CONNECTING  -> "Connecting" to MaterialTheme.colorScheme.tertiary
                                ConnectionState.ERROR       -> "Error"      to MaterialTheme.colorScheme.error
                                else                        -> "Offline"    to MaterialTheme.colorScheme.outline
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(text, style = MaterialTheme.typography.labelMedium, color = color)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "• $statusMessage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { viewModel.connect() },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Connect")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )

            if (isExpanded) {
                // Tablet: side-by-side
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(modifier = Modifier.weight(0.4f)) {
                        PatientSelectionCard(
                            selectedPatient = selectedPatient,
                            onClick = { showPatientSheet = true }
                        )
                        Spacer(Modifier.height(24.dp))
                        ActionPanel(
                            isCalculating = isCalculating,
                            connectionState = connectionState,
                            selectedTest = selectedTest,
                            selectedPatient = selectedPatient,
                            onSetBlank = { viewModel.setBlank() },
                            onRun = { selectedTest?.let { viewModel.performTest(it, selectedPatient) } }
                        )
                    }
                    Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                        AnalysisSection(
                            tests = tests,
                            selectedTest = selectedTest,
                            onTestSelect = { selectedTest = if (selectedTest == it) null else it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                // FIX 4: Phone — grid is weight(1f) so it fills available space cleanly,
                // ActionPanel stays pinned at the bottom without fighting for height
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PatientSelectionCard(
                        selectedPatient = selectedPatient,
                        onClick = { showPatientSheet = true }
                    )
                    Spacer(Modifier.height(16.dp))

                    AnalysisSection(
                        tests = tests,
                        selectedTest = selectedTest,
                        onTestSelect = { selectedTest = if (selectedTest == it) null else it },
                        modifier = Modifier.weight(1f)  // bounded height — grid won't over-measure
                    )

                    Spacer(Modifier.height(12.dp))

                    ActionPanel(
                        isCalculating = isCalculating,
                        connectionState = connectionState,
                        selectedTest = selectedTest,
                        selectedPatient = selectedPatient,
                        onSetBlank = { viewModel.setBlank() },
                        onRun = { selectedTest?.let { viewModel.performTest(it, selectedPatient) } },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }

    // FIX 5: Removed the broken `contentWindowInsets` lambda cast that could crash on
    // certain API levels. The default window insets handling is correct here.
    if (showPatientSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPatientSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            PatientSelectionSheet(
                patients = patients,
                onSelect = { selectedPatient = it; showPatientSheet = false }
            )
        }
    }
}

@Composable
fun PatientSelectionCard(selectedPatient: Patient?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (selectedPatient != null) Icons.Default.Person else Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    selectedPatient?.name ?: "Select Patient",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    selectedPatient?.let { "Age ${it.age} • ${it.gender}" }
                        ?: "Set patient for analysis record",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun AnalysisSection(
    tests: List<Test>,
    selectedTest: Test?,
    onTestSelect: (Test) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Analysis Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (tests.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "${tests.size} items",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (tests.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Science,
                message = "No tests configured. Go to the Tests tab to add one.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val columns = if (configuration.screenWidthDp > 800) 3 else 2
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                // FIX 6: fillMaxSize() is safe here because the parent Column has
                // weight(1f) which gives it a bounded, finite height constraint
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(tests) { test ->
                    AnalysisCard(
                        test = test,
                        isSelected = selectedTest == test,
                        onClick = { onTestSelect(test) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionPanel(
    isCalculating: Boolean,
    connectionState: ConnectionState,
    selectedTest: Test?,
    selectedPatient: Patient?,
    onSetBlank: () -> Unit,
    onRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(28.dp),
        border = if (isCalculating) null else androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AnimatedVisibility(visible = isCalculating) {
                Column {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSetBlank,
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = connectionState == ConnectionState.CONNECTED && !isCalculating,
                    shape = MaterialTheme.shapes.large
                ) { Text("Set Blank") }

                Button(
                    onClick = onRun,
                    modifier = Modifier.weight(1.2f).height(56.dp),
                    enabled = connectionState == ConnectionState.CONNECTED
                            && selectedTest != null && !isCalculating,
                    shape = MaterialTheme.shapes.large,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Run Test")
                }
            }
        }
    }
}

@Composable
fun AnalysisCard(test: Test, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Card(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 120.dp),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (isSelected) Icons.Default.CheckCircle else Icons.Default.Science,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(test.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                "Normal: ${test.minNormal}–${test.maxNormal}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PatientSelectionSheet(patients: List<Patient>, onSelect: (Patient) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPatients = remember(searchQuery, patients) {
        if (searchQuery.isBlank()) patients
        else patients.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 16.dp)
            .heightIn(max = 600.dp)
    ) {
        Text(
            "Select Patient",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            placeholder = { Text("Search by name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) } }
            } else null,
            shape = MaterialTheme.shapes.large,
            singleLine = true
        )

        when {
            patients.isEmpty() -> Box(modifier = Modifier.height(200.dp)) {
                EmptyState(icon = Icons.Default.PersonSearch, message = "No patients found. Add them in the Patients tab.")
            }
            filteredPatients.isEmpty() -> EmptyState(
                icon = Icons.Default.SearchOff,
                message = "No matches for \"$searchQuery\"",
                modifier = Modifier.padding(vertical = 48.dp)
            )
            else -> LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items = filteredPatients, key = { it.id }) { patient ->
                    Card(
                        onClick = { onSelect(patient) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        ListItem(
                            headlineContent = { Text(patient.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Age: ${patient.age} • ${patient.gender}") },
                            leadingContent = {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = patient.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}