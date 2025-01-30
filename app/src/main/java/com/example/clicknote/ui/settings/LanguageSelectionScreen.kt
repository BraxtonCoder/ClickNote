package com.example.clicknote.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.R
import com.example.clicknote.ui.components.TopBar

@Composable
fun LanguageSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: LanguageSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    var expandedGroups by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.language_selection),
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            LazyColumn {
                val filteredGroups = uiState.languageGroups.mapValues { (_, languages) ->
                    languages.filter { (_, name) ->
                        name.contains(searchQuery.text, ignoreCase = true)
                    }
                }.filter { it.value.isNotEmpty() }

                filteredGroups.forEach { (groupName, languages) ->
                    item {
                        LanguageGroupHeader(
                            groupName = groupName,
                            isExpanded = expandedGroups.contains(groupName),
                            onToggle = {
                                expandedGroups = if (expandedGroups.contains(groupName)) {
                                    expandedGroups - groupName
                                } else {
                                    expandedGroups + groupName
                                }
                            }
                        )
                    }

                    if (expandedGroups.contains(groupName)) {
                        items(languages) { (code, name) ->
                            LanguageItem(
                                languageCode = code,
                                languageName = name,
                                isSelected = code == uiState.selectedLanguage,
                                isRTL = uiState.rtlLanguages.contains(code),
                                onLanguageSelected = viewModel::setLanguage
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun LanguageGroupHeader(
    groupName: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
    }
}

@Composable
private fun LanguageItem(
    languageCode: String,
    languageName: String,
    isSelected: Boolean,
    isRTL: Boolean,
    onLanguageSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLanguageSelected(languageCode) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = languageName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = if (isRTL) androidx.compose.ui.text.style.TextAlign.End else androidx.compose.ui.text.style.TextAlign.Start
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.selected_language),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
} 