package com.taller.gemini.ui.screens

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.taller.gemini.ui.UiState
import com.taller.gemini.ui.components.AiActionButton
import com.taller.gemini.ui.components.ErrorCard
import com.taller.gemini.ui.components.IdleCard
import com.taller.gemini.ui.components.LoadingCard
import com.taller.gemini.ui.components.ResultCard
import com.taller.gemini.ui.components.ScreenColumn
import com.taller.gemini.ui.components.ScreenHeader
import com.taller.gemini.viewmodel.ImageViewModel

@Composable
fun ImageScreen(
    viewModel: ImageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    var promptInput by rememberSaveable { mutableStateOf("") }
    var pickerError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Android Modern Photo Picker (no requiere permisos de almacenamiento)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                viewModel.setSelectedBitmap(bitmap)
                pickerError = null
            } catch (e: Exception) {
                pickerError = "No se pudo abrir la imagen seleccionada. Intenta con otra."
            }
        }
    }

    val launchPicker = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    ScreenColumn {
        ScreenHeader(
            icon = Icons.Default.Image,
            title = "Consulta con Imagen (Multimodal)",
            description = "Selecciona una imagen de tu dispositivo y escribe una pregunta para que Gemini la analice."
        )

        if (selectedBitmap != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    bitmap = selectedBitmap!!.asImageBitmap(),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                OutlinedButton(onClick = { launchPicker() }) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Cambiar imagen")
                }
            }
        } else {
            Card(
                onClick = { launchPicker() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Toca para elegir una foto de la galería",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        pickerError?.let { ErrorCard(title = "Imagen no válida", message = it) }

        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            label = { Text("¿Qué deseas preguntar sobre la imagen?") },
            placeholder = { Text("Ej: ¿Qué objeto está dañado? / Describe esta foto") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            enabled = uiState !is UiState.Loading
        )

        AiActionButton(
            text = "Analizar Imagen",
            loadingText = "Analizando imagen...",
            icon = Icons.Default.AutoAwesome,
            isLoading = uiState is UiState.Loading,
            enabled = selectedBitmap != null,
            onClick = { viewModel.analyzeImage(promptInput) }
        )

        when (val state = uiState) {
            is UiState.Idle -> IdleCard(
                if (selectedBitmap == null) "Elige una foto arriba para habilitar el análisis."
                else "Presiona 'Analizar Imagen' para ver el resultado aquí."
            )
            is UiState.Loading -> LoadingCard("Procesando imagen con IA...")
            is UiState.Success -> ResultCard(
                icon = Icons.Default.AutoAwesome,
                title = "Resultado del Análisis",
                text = state.data
            )
            is UiState.Error -> ErrorCard(title = "Error en el Análisis", message = state.message)
        }
    }
}