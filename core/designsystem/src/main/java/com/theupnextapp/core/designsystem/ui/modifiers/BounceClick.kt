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

package com.theupnextapp.core.designsystem.ui.modifiers

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import com.theupnextapp.core.designsystem.ui.theme.LocalReducedMotion

/**
 * A modifier that provides a delightful squishy/bouncy interaction
 * mimicking physical springs when a user presses down.
 * When Reduced Motion / Calm Mode is enabled, bouncy scale is bypassed.
 */
fun Modifier.bounceClick(
    interactionSource: MutableInteractionSource? = null,
    role: Role? = null,
    onClick: () -> Unit,
) = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isReducedMotion = LocalReducedMotion.current

    if (isReducedMotion) {
        return@composed this.clickable(
            interactionSource = source,
            indication = LocalIndication.current,
            role = role,
            onClick = onClick,
        )
    }

    val isPressed by source.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.5f, // Medium Bouncy
                stiffness = 400f, // Low Stiffness
            ),
        label = "BounceClickScale",
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = source,
            indication = LocalIndication.current,
            role = role,
            onClick = onClick,
        )
}

/**
 * A modifier that scales content slightly on press using a physical spring,
 * respecting [LocalReducedMotion] to avoid discomfort for neurodivergent users.
 */
fun Modifier.bounceScale(
    interactionSource: InteractionSource,
): Modifier = composed {
    val isReducedMotion = LocalReducedMotion.current
    if (isReducedMotion) {
        return@composed this
    }

    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.5f,
                stiffness = 400f,
            ),
        label = "BounceScale",
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

