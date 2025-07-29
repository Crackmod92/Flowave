// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/components/FlowaveTopAppBar.kt
package com.crackmod.flowave.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowaveTopAppBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null, // <-- ИЗМЕНЕНИЕ: Сделали параметр необязательным
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    isSearchable: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    isSearchActive: Boolean = false,
    onToggleSearch: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        } else {
            keyboardController?.hide()
        }
    }

    Crossfade(
        targetState = isSearchActive,
        label = "top_app_bar_crossfade",
        animationSpec = tween(durationMillis = 300)
    ) { searchActive ->
        if (searchActive) {
            // Режим поиска
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("Поиск...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onToggleSearch()
                        onSearchQueryChange("")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Закрыть поиск")
                    }
                },
                actions = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить поиск")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        } else {
            // Стандартный режим
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                // <-- ИЗМЕНЕНИЕ: Вызываем иконку, только если она передана
                navigationIcon = { navigationIcon?.invoke() },
                actions = {
                    if (isSearchable) {
                        IconButton(onClick = onToggleSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Поиск")
                        }
                    }
                    actions()
                },
                scrollBehavior = scrollBehavior,
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    }
}