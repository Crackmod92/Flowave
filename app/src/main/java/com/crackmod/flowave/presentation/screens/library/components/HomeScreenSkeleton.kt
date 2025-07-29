package com.crackmod.flowave.presentation.screens.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Категории
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryCardPlaceholder(modifier = Modifier.weight(1f))
                CategoryCardPlaceholder(modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryCardPlaceholder(modifier = Modifier.weight(1f))
                CategoryCardPlaceholder(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Секции треков
        repeat(2) {
            SectionHeaderPlaceholder()
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) {
                    CompactTrackCardPlaceholder()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlaceholderBox(modifier: Modifier) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant))
}

@Composable
private fun CategoryCardPlaceholder(modifier: Modifier = Modifier) {
    PlaceholderBox(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}

@Composable
private fun SectionHeaderPlaceholder() {
    PlaceholderBox(
        modifier = Modifier
            .padding(start = 16.dp, end = 80.dp, top = 24.dp, bottom = 8.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}

@Composable
private fun CompactTrackCardPlaceholder() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PlaceholderBox(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        PlaceholderBox(
            modifier = Modifier
                .width(120.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        PlaceholderBox(
            modifier = Modifier
                .width(80.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}