package com.example.chemistryanalyser.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chemistryanalyser.ui.TestViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ResultsScreen(viewModel: TestViewModel) {
    val results by viewModel.allResults.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(results) { result ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(result.testName, style = MaterialTheme.typography.titleLarge)
                        Text(dateFormat.format(Date(result.timestamp)), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Patient: ${result.patientName ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column {
                            Text("Absorbance", style = MaterialTheme.typography.labelMedium)
                            Text(String.format("%.4f", result.absorbance), style = MaterialTheme.typography.bodyLarge)
                        }
                        Column {
                            Text("Concentration", style = MaterialTheme.typography.labelMedium)
                            Text(String.format("%.2f", result.concentration), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Wavelength", style = MaterialTheme.typography.labelMedium)
                            Text("${result.wavelength} nm", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
