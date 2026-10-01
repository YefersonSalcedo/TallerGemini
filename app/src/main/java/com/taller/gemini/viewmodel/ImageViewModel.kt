package com.taller.gemini.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taller.gemini.data.AiRepository
import com.taller.gemini.di.AppModule
import com.taller.gemini.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageViewModel(
    private val repository: AiRepository = AppModule.aiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    fun setSelectedBitmap(bitmap: Bitmap?) {
        _selectedBitmap.value = bitmap
        _uiState.value = UiState.Idle
    }

    fun analyzeImage(prompt: String) {
        val bitmap = _selectedBitmap.value
        if (bitmap == null) {
            _uiState.value = UiState.Error("Primero debes seleccionar una imagen de la galería.")
            return
        }

        val effectivePrompt = if (prompt.isBlank()) "¿Qué ves en esta imagen? Describe los detalles clave." else prompt

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.askAboutImage(bitmap, effectivePrompt)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = UiState.Success(response)
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.localizedMessage ?: "Error al procesar la imagen con Gemini")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
