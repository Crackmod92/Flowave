package com.crackmod.flowave.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.domain.repository.ThemeSetting
import com.crackmod.flowave.presentation.screens.settings.SettingsViewModel

// ### ИЗМЕНЕНИЕ: Обновляем lightColorScheme ###
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    inverseOnSurface = LightOnBackground,
    inverseSurface = LightSurface,
    tertiary = LightPrimary,
    onTertiary = LightOnPrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkPrimary,
    onSecondary = DarkOnPrimary,
    secondaryContainer = DarkPrimaryContainer,
    onSecondaryContainer = DarkOnPrimaryContainer,
    tertiary = DarkPrimary,
    onTertiary = DarkOnPrimary,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val AmoledColorScheme = ColorScheme(
    primary = AmoledPrimary,
    onPrimary = AmoledOnPrimary,
    primaryContainer = AmoledPrimaryContainer,
    onPrimaryContainer = AmoledOnPrimaryContainer,
    inversePrimary = DarkPrimary,
    secondary = AmoledPrimary,
    onSecondary = AmoledOnPrimary,
    secondaryContainer = AmoledPrimaryContainer,
    onSecondaryContainer = AmoledOnPrimaryContainer,
    tertiary = AmoledPrimary,
    onTertiary = AmoledOnPrimary,
    tertiaryContainer = AmoledPrimaryContainer,
    onTertiaryContainer = AmoledOnPrimaryContainer,
    background = AmoledBackground,
    onBackground = AmoledOnBackground,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledOnSurfaceVariant,
    surfaceTint = Color.Black,
    inverseSurface = AmoledOnBackground,
    inverseOnSurface = AmoledBackground,
    error = AmoledError,
    onError = AmoledOnError,
    errorContainer = AmoledErrorContainer,
    onErrorContainer = AmoledOnErrorContainer,
    outline = AmoledOutline,
    outlineVariant = AmoledOutlineVariant,
    scrim = Color.Black,
    surfaceBright = AmoledBackground,
    surfaceDim = AmoledBackground,
    surfaceContainer = AmoledBackground,
    surfaceContainerHigh = AmoledBackground,
    surfaceContainerHighest = AmoledBackground,
    surfaceContainerLow = AmoledBackground,
    surfaceContainerLowest = AmoledBackground
)

@Composable
fun FlowaveTheme(
    content: @Composable () -> Unit
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currentThemeSetting by settingsViewModel.themeSetting.collectAsState()

    val colorScheme = when (currentThemeSetting) {
        ThemeSetting.SYSTEM -> {
            if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
        }
        ThemeSetting.LIGHT -> LightColorScheme
        ThemeSetting.DARK -> DarkColorScheme
        ThemeSetting.AMOLED -> AmoledColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun getNavigationIconColors(): Pair<Color, Color> {
    val currentScheme = MaterialTheme.colorScheme
    return if (currentScheme.background == AmoledBackground) { // AMOLED
        NavigationSelectedAmoled to NavigationUnselectedAmoled
    } else if(currentScheme.isLight()) { // Light
        NavigationSelectedLight to NavigationUnselectedLight
    } else { // Dark
        DarkPrimary to NavigationUnselectedDark
    }
}

@Composable
private fun ColorScheme.isLight() = this.background.luminance() > 0.5