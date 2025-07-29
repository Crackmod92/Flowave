package com.crackmod.flowave.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================
// ФИРМЕННАЯ ПАЛИТРА FLOWAVE
// =============================================================

// === СВЕТЛАЯ ТЕМА (Цвет книжных страниц) ===
val LightPrimary = Color(0xFF005AC1) // Не тронут
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFD8E2FF)
val LightOnPrimaryContainer = Color(0xFF001A41) // Не тронут

val LightBackground = Color(0xFFE8E2D9) // Цвет старой бумаги
val LightOnBackground = Color(0xFF26221D) // Тёмно-коричнево-серый

val LightSurface = Color(0xFFE4DED5) // Цвет страницы с легким оттенком
val LightOnSurface = Color(0xFF2B2722) // Приглушённый тёмно-коричневый

val LightSurfaceVariant = Color(0xFFDAD4CB) // Более тёмный оттенок бумаги
val LightOnSurfaceVariant = Color(0xFF595550) // Тёплый серо-коричневый

val LightOutline = Color(0xFFB4AEAA) // Тёплый серо-коричневый контур
val LightOutlineVariant = Color(0xFFCBC5C0) // Светло-коричневый контур

val LightError = Color(0xFFB3261E) // Тёплый красный
val LightOnError = Color.White
val LightErrorContainer = Color(0xFFEED3D0) // Светло-розоватый, как выделение в книге
val LightOnErrorContainer = Color(0xFF410002)


// === ТЁМНАЯ ТЕМА ===
val DarkPrimary = Color(0xFFADC6FF)
val DarkOnPrimary = Color(0xFF002E69)
val DarkPrimaryContainer = Color(0xFF004494)
val DarkOnPrimaryContainer = Color(0xFFD8E2FF)
val DarkBackground = Color(0xFF1B1B1F)
val DarkOnBackground = Color(0xFFE3E2E6)
val DarkSurface = Color(0xFF1B1B1F)
val DarkOnSurface = Color(0xFFE3E2E6)
val DarkSurfaceVariant = Color(0xFF44474F)
val DarkOnSurfaceVariant = Color(0xFFC4C6D0)
val DarkOutline = Color(0xFF8E9099)
val DarkOutlineVariant = Color(0xFF44474F)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)


// #############################################################
// ### AMOLED ТЕМА (ФИНАЛЬНАЯ ВЕРСИЯ) ###
// #############################################################

val AmoledPrimary = Color.White
val AmoledOnPrimary = Color.Black
val AmoledPrimaryContainer = Color.Black
val AmoledOnPrimaryContainer = Color.White
val AmoledBackground = Color.Black
val AmoledOnBackground = Color.White
val AmoledSurface = Color.Black
val AmoledOnSurface = Color.White
val AmoledSurfaceVariant = Color.Black
val AmoledOnSurfaceVariant = Color(0xFFCCCCCC) // Приглушенный белый для второстепенного текста
val AmoledOutline = Color(0xFF444444)
val AmoledOutlineVariant = Color(0xFF2C2C2C)
val AmoledError = Color(0xFFFF8989)
val AmoledOnError = Color.Black
val AmoledErrorContainer = Color(0xFF93000A)
val AmoledOnErrorContainer = Color(0xFFFFDAD6)

// === ЦВЕТА НАВИГАЦИИ ===
val NavigationSelectedLight = LightPrimary
val NavigationUnselectedLight = Color(0xFF6B6B6B)

val NavigationSelectedDark = DarkPrimary
val NavigationUnselectedDark = Color(0xFF9E9E9E)

val NavigationSelectedAmoled = AmoledPrimary
// ### ИЗМЕНЕНИЕ: Отдельный цвет для неактивных иконок в AMOLED ###
val NavigationUnselectedAmoled = Color(0xFF8A8A8A)