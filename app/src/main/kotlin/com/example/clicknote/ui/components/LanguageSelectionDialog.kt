package com.example.clicknote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*

data class Language(
    val code: String,
    val name: String,
    val nativeName: String
) {
    companion object {
        fun getAvailableLanguages(): List<Language> = listOf(
            Language("en", "English", "English"),
            Language("es", "Spanish", "Español"),
            Language("fr", "French", "Français"),
            Language("de", "German", "Deutsch"),
            Language("it", "Italian", "Italiano"),
            Language("pt", "Portuguese", "Português"),
            Language("ru", "Russian", "Русский"),
            Language("ja", "Japanese", "日本語"),
            Language("ko", "Korean", "한국어"),
            Language("zh", "Chinese", "中文"),
            Language("ar", "Arabic", "العربية"),
            Language("hi", "Hindi", "हिन्दी"),
            Language("bn", "Bengali", "বাংলা"),
            Language("tr", "Turkish", "Türkçe"),
            Language("vi", "Vietnamese", "Tiếng Việt")
        ).sortedBy { it.name }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val languages = remember { Language.getAvailableLanguages() }
    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) languages
        else languages.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.nativeName.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search languages") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                LazyColumn {
                    items(filteredLanguages) { language ->
                        ListItem(
                            headlineContent = { Text(language.name) },
                            supportingContent = { Text(language.nativeName) },
                            trailingContent = if (language.code == currentLanguage) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = language.code == currentLanguage,
                                    onClick = {
                                        onLanguageSelected(language.code)
                                        onDismiss()
                                    }
                                )
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 