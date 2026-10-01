package com.taller.gemini.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.taller.gemini.ui.UiState
import com.taller.gemini.ui.components.AiActionButton
import com.taller.gemini.ui.components.ErrorCard
import com.taller.gemini.ui.components.IdleCard
import com.taller.gemini.ui.components.LoadingCard
import com.taller.gemini.ui.components.ResultCard
import com.taller.gemini.ui.components.ScreenColumn
import com.taller.gemini.ui.components.ScreenHeader
import com.taller.gemini.ui.components.SuggestionRow
import com.taller.gemini.viewmodel.TextViewModel

@Composable
fun TextScreen(
    viewModel: TextViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var promptInput by rememberSaveable { mutableStateOf("") }

    val suggestions = listOf(
        "¿Qué ventajas ofrece Jetpack Compose?",
        "Explica el patrón Repository en Kotlin",
        "Dame 3 consejos de seguridad para almacenar API Keys"
    )

    ScreenColumn {
        ScreenHeader(
            icon = Icons.Default.TextFields,
            title = "Consulta de Texto",
            description = "Escribe una pregunta y el modelo de IA te responderá."
        )

        SuggestionRow(suggestions) { promptInput = it }

        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            label = { Text("Escribe tu pregunta...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            enabled = uiState !is UiState.Loading
        )

        AiActionButton(
            text = "Enviar Pregunta",
            loadingText = "Consultando IA...",
            icon = Icons.AutoMirrored.Filled.Send,
            isLoading = uiState is UiState.Loading,
            enabled = promptInput.isNotBlank(),
            onClick = { viewModel.ask(promptInput) }
        )

        when (val state = uiState) {
            is UiState.Idle -> IdleCard("Presiona 'Enviar Pregunta' para ver la respuesta aquí.")
            is UiState.Loading -> LoadingCard("Generando respuesta...")
            is UiState.Success -> ResultCard(
                icon = Icons.Default.AutoAwesome,
                title = "Respuesta del Modelo",
                text = state.data
            )
            is UiState.Error -> ErrorCard(title = "Ocurrió un Error", message = state.message)
        }
    }
}