package com.example.myapplication.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal for providing the [SharedTransitionScope] down the UI tree.
 * This allows components to participate in shared element transitions without
 * having to pass the scope as a parameter.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/**
 * CompositionLocal for providing the [AnimatedVisibilityScope] down the UI tree.
 * This is typically provided by [AnimatedContent] or [AnimatedVisibility].
 */
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
