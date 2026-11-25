/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 */

package com.theupnextapp.core.designsystem.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.theupnextapp.core.designsystem.ui.theme.LocalReducedMotion

/**
 * An animated toggle button for marking episodes as watched/unwatched.
 * Provides a tactile spring bounce and color transition when toggled,
 * automatically respecting [LocalReducedMotion] for neurodivergent and sensory-sensitive users.
 */
@Suppress("MagicNumber")
@Composable
fun AnimatedWatchedToggleButton(
    isWatched: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val isReducedMotion = LocalReducedMotion.current
    val scaleAnim = remember { Animatable(1f) }
    var isInitial by remember { mutableStateOf(true) }

    LaunchedEffect(isWatched) {
        if (isInitial) {
            isInitial = false
            return@LaunchedEffect
        }
        if (!isReducedMotion) {
            scaleAnim.snapTo(0.75f)
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec =
                    spring(
                        dampingRatio = 0.4f,
                        stiffness = 300f,
                    ),
            )
        }
    }

    val iconTint by animateColorAsState(
        targetValue =
            if (isWatched) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec =
            if (isReducedMotion) {
                tween(durationMillis = 150)
            } else {
                tween(durationMillis = 250)
            },
        label = "WatchedToggleTint",
    )

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onToggle()
        },
        modifier = modifier.testTag("animated_watched_toggle_button"),
        enabled = enabled,
    ) {
        Icon(
            imageVector = if (isWatched) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier =
                Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                    },
        )
    }
}

/**
 * An animated toggle button for adding/removing shows from a watchlist.
 * Provides a tactile spring bounce and color transition when toggled,
 * automatically respecting [LocalReducedMotion].
 */
@Suppress("MagicNumber")
@Composable
fun AnimatedWatchlistIconButton(
    isWatchlist: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val isReducedMotion = LocalReducedMotion.current
    val scaleAnim = remember { Animatable(1f) }
    var isInitial by remember { mutableStateOf(true) }

    LaunchedEffect(isWatchlist) {
        if (isInitial) {
            isInitial = false
            return@LaunchedEffect
        }
        if (!isReducedMotion) {
            scaleAnim.snapTo(0.75f)
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec =
                    spring(
                        dampingRatio = 0.4f,
                        stiffness = 300f,
                    ),
            )
        }
    }

    val iconTint by animateColorAsState(
        targetValue =
            if (isWatchlist) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec =
            if (isReducedMotion) {
                tween(durationMillis = 150)
            } else {
                tween(durationMillis = 250)
            },
        label = "WatchlistToggleTint",
    )

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onToggle()
        },
        modifier = modifier.testTag("animated_watchlist_toggle_button"),
        enabled = enabled,
    ) {
        Icon(
            imageVector = if (isWatchlist) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier =
                Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                    },
        )
    }
}
