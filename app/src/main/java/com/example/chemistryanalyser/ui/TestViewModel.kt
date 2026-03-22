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

    private val _statusMessage = MutableStateFlow("Tap 'Connect' to start")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _latestResult = MutableStateFlow<TestResult?>(null)
    val latestResult: StateFlow<TestResult?> = _latestResult.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    fun connect() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.CONNECTING
            _statusMessage.value = "Connecting to ChemistryAnalyser..."
            try {
                val response = repository.getStatus()
                if (response.connected || response.status == "ready") {
                    _connectionState.value = ConnectionState.CONNECTED
                    _statusMessage.value = "Connected to Analyser"
                } else {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _statusMessage.value = "Analyser is busy"
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                _statusMessage.value = "Failed to connect. Check Wi-Fi."
            }
        }
    }

    fun checkStatus() = connect()

    fun setBlank() {
        viewModelScope.launch {
            try {
                repository.setBlank()
                _statusMessage.value = "Blank calibration successful"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to set blank"
            }
        }
    }

    fun performTest(test: Test, patient: Patient?) {
        viewModelScope.launch {
            _isCalculating.value = true
            _statusMessage.value = "Running ${test.name}..."
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
                    wavelength = test.optimalWavelength
                )
                repository.saveResult(result)
                _latestResult.value = result
                _statusMessage.value = "${test.name} complete: ${String.format("%.2f", concentration)}"
            } catch (e: Exception) {
                _statusMessage.value = "Error during test: ${e.message}"
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
    
    fun calculateAndSaveLipidExtras(patient: Patient?, totalChol: Double, ldl: Double, trig: Double) {
        val vldl = trig / 5.0
        val hdl = totalChol - ldl - vldl
        
        viewModelScope.launch {
            val vldlResult = TestResult(
                patientId = patient?.id,
                patientName = patient?.name ?: "Walk-in",
                testId = "vldl",
                testName = "VLDL (Calculated)",
                absorbance = 0.0,
                concentration = vldl,
                wavelength = 0
            )
            val hdlResult = TestResult(
                patientId = patient?.id,
                patientName = patient?.name ?: "Walk-in",
                testId = "hdl",
                testName = "HDL (Calculated)",
                absorbance = 0.0,
                concentration = hdl,
                wavelength = 0
            )
            repository.saveResult(vldlResult)
            repository.saveResult(hdlResult)
        }
    }
}
