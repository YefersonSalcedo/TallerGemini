package com.taller.gemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taller.gemini.data.AiRepository
import com.taller.gemini.di.AppModule
import com.taller.gemini.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TextViewModel(
    private val repository: AiRepository = AppModule.aiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    fun ask(prompt: String) {
        if (prompt.isBlank()) {
            _uiState.value = UiState.Error("Por favor ingresa una pregunta o instrucción.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.askText(prompt)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = UiState.Success(response)
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.localizedMessage ?: "Error desconocido al consultar Gemini")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
