package com.example.chemistryanalyser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chemistryanalyser.data.model.Patient
import com.example.chemistryanalyser.data.model.Test
import com.example.chemistryanalyser.data.model.TestResult
import com.example.chemistryanalyser.data.repository.TestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

class TestViewModel(private val repository: TestRepository) : ViewModel() {

    val allTests = repository.allTests
    val allPatients = repository.allPatients
    val allResults = repository.allResults

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _statusMessage = MutableStateFlow("Tap 'Link' to start")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    fun connect() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.CONNECTING
            _statusMessage.value = "Searching for Analyser..."
            try {
                val response = repository.getStatus()
                if (response.connected || response.status == "ready") {
                    _connectionState.value = ConnectionState.CONNECTED
                    _statusMessage.value = "System Ready"
                } else {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _statusMessage.value = "Analyser Busy"
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                _statusMessage.value = e.javaClass.simpleName + ": " + (e.message ?: "null")
            }
        }
    }

    fun setBlank() {
        viewModelScope.launch {
            try {
                repository.setBlank()
                _statusMessage.value = "Blank calibrated"
            } catch (e: Exception) {
                _statusMessage.value = "Calibration Error"
            }
        }
    }

    fun performTest(test: Test, patient: Patient?) {
        viewModelScope.launch {
            _isCalculating.value = true
            _statusMessage.value = "Analyzing ${test.name}..."
            try {
                val response = repository.calculate(test.optimalWavelength)
                val absorbance = response.absorbance
                
                val concentration = if (test.standardAbsorbance != 0.0) {
                    (absorbance / test.standardAbsorbance) * test.standardConcentration
                } else 0.0
                
                val result = TestResult(
                    patientId = patient?.id,
                    patientName = patient?.name ?: "Walk-in",
                    testId = test.id,
                    testName = test.name,
                    absorbance = absorbance,
                    concentration = concentration,
                    wavelength = test.optimalWavelength,
                    minNormal = test.minNormal,
                    maxNormal = test.maxNormal,
                    unit = test.unit
                )
                repository.saveResult(result)
                _statusMessage.value = "${test.name}: ${String.format("%.2f", concentration)} ${test.unit}"
            } catch (e: Exception) {
                _statusMessage.value = "Analysis failed"
            } finally {
                _isCalculating.value = false
            }
        }
    }

    fun addTest(test: Test) {
        viewModelScope.launch {
            repository.insertTest(test)
        }
    }

    fun addPatient(name: String, age: Int, gender: String) {
        viewModelScope.launch {
            repository.insertPatient(Patient(name = name, age = age, gender = gender))
        }
    }
}
