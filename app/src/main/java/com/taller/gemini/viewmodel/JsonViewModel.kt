package com.taller.gemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taller.gemini.data.AiRepository
import com.taller.gemini.di.AppModule
import com.taller.gemini.model.IncidentInfo
import com.taller.gemini.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JsonViewModel(
    private val repository: AiRepository = AppModule.aiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<IncidentInfo>>(UiState.Idle)
    val uiState: StateFlow<UiState<IncidentInfo>> = _uiState.asStateFlow()

    fun classify(description: String) {
        if (description.isBlank()) {
            _uiState.value = UiState.Error("Ingresa una descripción del reporte o problema urbano.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.classifyIncident(description)
            result.fold(
                onSuccess = { incident ->
                    _uiState.value = UiState.Success(incident)
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.localizedMessage ?: "Error al clasificar la incidencia")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
