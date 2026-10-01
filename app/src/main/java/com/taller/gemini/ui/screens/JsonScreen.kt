package com.taller.gemini.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taller.gemini.model.IncidentInfo
import com.taller.gemini.ui.UiState
import com.taller.gemini.ui.components.AiActionButton
import com.taller.gemini.ui.components.ErrorCard
import com.taller.gemini.ui.components.IdleCard
import com.taller.gemini.ui.components.LoadingCard
import com.taller.gemini.ui.components.ScreenColumn
import com.taller.gemini.ui.components.ScreenHeader
import com.taller.gemini.ui.components.SuggestionRow
import com.taller.gemini.ui.theme.UrgencyCritical
import com.taller.gemini.ui.theme.UrgencyCriticalBg
import com.taller.gemini.ui.theme.UrgencyHigh
import com.taller.gemini.ui.theme.UrgencyHighBg
import com.taller.gemini.ui.theme.UrgencyLow
import com.taller.gemini.ui.theme.UrgencyLowBg
import com.taller.gemini.ui.theme.UrgencyMedium
import com.taller.gemini.ui.theme.UrgencyMediumBg
import com.taller.gemini.viewmodel.JsonViewModel

@Composable
fun JsonScreen(
    viewModel: JsonViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var incidentText by rememberSaveable { mutableStateOf("") }

    val examples = listOf(
        "Fuga masiva de agua potable inundando Av. Central",
        "Bache profundo en el cruce de Independencia dañando vehículos",
        "Poste de luz roto con cables caídos cerca de la escuela primaria",
        "Basura acumulada en el parque impidiendo el paso peatonal"
    )

    ScreenColumn {
        ScreenHeader(
            icon = Icons.Default.DataObject,
            title = "Extracción de JSON Estructurado",
            description = "Describe una incidencia ciudadana y la IA la clasificará por categoría, urgencia y resumen."
        )

        SuggestionRow(examples) { incidentText = it }

        OutlinedTextField(
            value = incidentText,
            onValueChange = { incidentText = it },
            label = { Text("Descripción del reporte urbano...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            enabled = uiState !is UiState.Loading
        )

        AiActionButton(
            text = "Clasificar Incidencia",
            loadingText = "Clasificando con IA...",
            icon = Icons.Default.ReportProblem,
            isLoading = uiState is UiState.Loading,
            enabled = incidentText.isNotBlank(),
            onClick = { viewModel.classify(incidentText) }
        )

        when (val state = uiState) {
            is UiState.Idle -> IdleCard("Presiona 'Clasificar Incidencia' para ver los datos estructurados.")
            is UiState.Loading -> LoadingCard("Analizando reporte y estructurando JSON...")
            is UiState.Success -> IncidentCard(incident = state.data)
            is UiState.Error -> ErrorCard(title = "Error de Clasificación", message = state.message)
        }
    }
}

@Composable
private fun IncidentCard(incident: IncidentInfo) {
    val (urgenciaColor, urgenciaBg) = when (incident.urgencia.lowercase()) {
        "crítica", "critica" -> UrgencyCritical to UrgencyCriticalBg
        "alta" -> UrgencyHigh to UrgencyHighBg
        "media" -> UrgencyMedium to UrgencyMediumBg
        else -> UrgencyLow to UrgencyLowBg
    }

    // Tarjeta blanca con borde: se distingue del fondo sin depender de la sombra
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CATEGORÍA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = incident.categoria,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Surface(shape = RoundedCornerShape(16.dp), color = urgenciaBg) {
                    Text(
                        text = "Urgencia: ${incident.urgencia.uppercase()}",
                        color = urgenciaColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "RESUMEN EJECUTIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = incident.resumen,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}