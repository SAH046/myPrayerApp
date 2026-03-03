package com.example.myapplication.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

/**
 * Represents the different categories of prayer prerequisites (Shurut al-Salat).
 *
 * @property id Unique identifier for the prerequisite type, used for shared element transitions.
 */
enum class PrerequisiteType(val id: String) {
    WUDU("wudu"), CLOTHING("clothing"), QIBLA("qibla"), TIME("time")
}

/**
 * Data class representing an individual prerequisite item with multilingual support.
 *
 * @property type The [PrerequisiteType] of the item.
 * @property titleDe The title in German.
 * @property titleTr The title in Turkish.
 * @property descriptionDe The description in German.
 * @property descriptionTr The description in Turkish.
 * @property icon The [ImageVector] to display for this item.
 */
data class PrerequisiteItem(
    val type: PrerequisiteType,
    val titleDe: String,
    val titleTr: String,
    val descriptionDe: String,
    val descriptionTr: String,
    val icon: ImageVector = Icons.Default.CheckCircle
)

/**
 * The main screen for displaying prayer prerequisites.
 *
 * This screen manages the state of the selected prerequisite and handles navigation
 * using shared element transitions. It also integrates with [PredictiveBackHandler]
 * for a smooth gesture-based back navigation.
 *
 * @param modifier The modifier to be applied to the layout.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PrerequisitesScreen(modifier: Modifier = Modifier) {
    val lang = LocalAppLanguage.current
    var selectedPrerequisite by remember { mutableStateOf<PrerequisiteType?>(null) }
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")

    val transitionState = remember { SeekableTransitionState(selectedPrerequisite) }
    LaunchedEffect(selectedPrerequisite) {
        transitionState.animateTo(selectedPrerequisite)
    }

    PredictiveBackHandler(enabled = selectedPrerequisite != null) { progress ->
        try {
            progress.collect { backEvent ->
                transitionState.seekTo(backEvent.progress, targetState = null)
            }
            selectedPrerequisite = null
        } catch (e: CancellationException) {
            transitionState.animateTo(selectedPrerequisite)
        }
    }

    val items = remember {
        listOf(
            PrerequisiteItem(
                PrerequisiteType.WUDU,
                "Wudu (Gebetswaschung)", "Abdest",
                "Die rituelle Reinigung vor dem Gebet.", "Namazdan önce alınan abdest."
            ),
            PrerequisiteItem(
                PrerequisiteType.CLOTHING,
                "Kleidung (Awra)", "Setr-i Avret",
                "Bedeckung des Körpers.", "Vücudun örtülmesi gereken yerlerinin örtülmesi."
            ),
            PrerequisiteItem(
                PrerequisiteType.QIBLA,
                "Gebetsrichtung (Qibla)", "Kıble",
                "Ausrichtung nach Mekka.", "Kabe'ye yönelmek."
            ),
            PrerequisiteItem(
                PrerequisiteType.TIME,
                "Zeit", "Vakit",
                "Das Gebet zur richtigen Zeit verrichten.", "Namazın vaktinde kılınması."
            )
        )
    }

    with(sharedTransitionScope) {
        val transition = rememberTransition(transitionState, label = "PrerequisiteTransition")
        transition.AnimatedContent(
            transitionSpec = {
                if (targetState != null) {
                    (fadeIn(tween(400)) + scaleIn(initialScale = 0.85f, animationSpec = tween(400))) togetherWith (fadeOut(tween(200)) + scaleOut(targetScale = 1.15f, animationSpec = tween(400)))
                } else {
                    (fadeIn(tween(400)) + scaleIn(initialScale = 1.15f, animationSpec = tween(400))) togetherWith (fadeOut(tween(200)) + scaleOut(targetScale = 0.85f, animationSpec = tween(400)))
                }
            }
        ) { targetType ->
            if (targetType == null) {
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedContent) {
                    PrerequisiteSelectionScreen(
                        title = if (lang == AppLanguage.GERMAN) "Voraussetzungen" else "Ön Şartlar",
                        items = items,
                        onItemSelected = { selectedPrerequisite = it }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            rememberSharedContentState(key = "prereq_${targetType.id}"),
                            animatedVisibilityScope = this@AnimatedContent
                        )
                ) {
                    when (targetType) {
                        PrerequisiteType.WUDU -> WuduGuideScreen(
                            onBack = { selectedPrerequisite = null },
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> PrerequisitePlaceholder(targetType, lang) { selectedPrerequisite = null }
                    }
                }
            }
        }
    }
}

/**
 * Displays the selection list of prerequisites.
 *
 * @param title The localized title of the screen.
 * @param items The list of [PrerequisiteItem] to display.
 * @param onItemSelected Callback triggered when an item is clicked.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PrerequisiteSelectionScreen(
    title: String,
    items: List<PrerequisiteItem>,
    onItemSelected: (PrerequisiteType) -> Unit
) {
    val lang = LocalAppLanguage.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No AnimatedVisibilityScope found")

    with(sharedTransitionScope) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp) // Added bottom padding
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedBounds(
                                rememberSharedContentState(key = "prereq_${item.type.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .clickable { onItemSelected(item.type) },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        item.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (lang == AppLanguage.GERMAN) item.titleDe else item.titleTr,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (lang == AppLanguage.GERMAN) item.descriptionDe else item.descriptionTr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A placeholder screen for prerequisites that are not yet implemented.
 *
 * @param type The type of prerequisite being displayed.
 * @param lang The current application language.
 * @param onBack Callback for navigating back to the selection screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrerequisitePlaceholder(type: PrerequisiteType, lang: AppLanguage, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(
            title = { Text(type.name, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                if (lang == AppLanguage.GERMAN) "In Kürze verfügbar..." else "Yakında eklenecek...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
