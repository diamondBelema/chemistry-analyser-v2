package com.example.chemistryanalyser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chemistryanalyser.ui.TestViewModel
import com.example.chemistryanalyser.ui.ViewModelFactory
import com.example.chemistryanalyser.ui.screens.HomeScreen
import com.example.chemistryanalyser.ui.screens.PatientScreen
import com.example.chemistryanalyser.ui.screens.ResultsScreen
import com.example.chemistryanalyser.ui.screens.TestsScreen
import com.example.chemistryanalyser.ui.theme.ChemistryAnalyserTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TestViewModel by viewModels {
        ViewModelFactory((application as ChemistryAnalyserApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChemistryAnalyserTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TestViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chemistry Analyser") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Tests") },
                    label = { Text("Tests") },
                    selected = currentRoute == "tests",
                    onClick = { navController.navigate("tests") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Patients") },
                    label = { Text("Patients") },
                    selected = currentRoute == "patients",
                    onClick = { navController.navigate("patients") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Results") },
                    label = { Text("Results") },
                    selected = currentRoute == "results",
                    onClick = { navController.navigate("results") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(viewModel) }
            composable("tests") { TestsScreen(viewModel) }
            composable("patients") { PatientScreen(viewModel) }
            composable("results") { ResultsScreen(viewModel) }
        }
    }
}
