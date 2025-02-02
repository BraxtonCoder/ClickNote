package com.example.clicknote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

data class FolderColor(
    val name: String,
    val color: Color
)

val folderColors = listOf(
    FolderColor("Red", Color(0xFFE57373)),
    FolderColor("Pink", Color(0xFFF06292)),
    FolderColor("Purple", Color(0xFFBA68C8)),
    FolderColor("Deep Purple", Color(0xFF9575CD)),
    FolderColor("Indigo", Color(0xFF7986CB)),
    FolderColor("Blue", Color(0xFF64B5F6)),
    FolderColor("Light Blue", Color(0xFF4FC3F7)),
    FolderColor("Cyan", Color(0xFF4DD0E1)),
    FolderColor("Teal", Color(0xFF4DB6AC)),
    FolderColor("Green", Color(0xFF81C784)),
    FolderColor("Light Green", Color(0xFFAED581)),
    FolderColor("Lime", Color(0xFFDCE775)),
    FolderColor("Yellow", Color(0xFFFFD54F)),
    FolderColor("Amber", Color(0xFFFFB74D)),
    FolderColor("Orange", Color(0xFFFFB74D)),
    FolderColor("Deep Orange", Color(0xFFFF8A65)),
    FolderColor("Brown", Color(0xFFA1887F)),
    FolderColor("Grey", Color(0xFF90A4AE)),
    FolderColor("Blue Grey", Color(0xFF78909C)),
    FolderColor("Black", Color(0xFF424242))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Color preview button
    Surface(
        onClick = { showDialog = true },
        shape = CircleShape,
        modifier = modifier
            .size(40.dp)
            .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(selectedColor)
        )
    }
    
    // Color picker dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose folder color") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(folderColors) { folderColor ->
                        ColorItem(
                            color = folderColor.color,
                            isSelected = selectedColor.toArgb() == folderColor.color.toArgb(),
                            onClick = {
                                onColorSelected(folderColor.color)
                                showDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White
            )
        }
    }
} 