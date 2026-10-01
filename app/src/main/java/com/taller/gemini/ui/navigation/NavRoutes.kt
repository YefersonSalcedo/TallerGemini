package com.taller.gemini.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Text : Screen("text", "Texto", Icons.Default.TextFields)
    data object Image : Screen("image", "Imagen", Icons.Default.Image)
    data object Json : Screen("json", "JSON", Icons.Default.DataObject)
}
