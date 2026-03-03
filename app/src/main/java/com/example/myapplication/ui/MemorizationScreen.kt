package com.example.myapplication.ui

import android.speech.tts.TextToSpeech
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.MemorizationCategory
import com.example.myapplication.data.MemorizationDataProvider
import com.example.myapplication.data.MemorizationItem
import kotlinx.coroutines.CancellationException
import java.util.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MemorizationScreen(
    tts: TextToSpeech?,
    isTtsReady: Boolean,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val categories = remember(lang) { MemorizationDataProvider.getMemorizationData(lang) }
    var selectedCategory by remember { mutableStateOf<MemorizationCategory?>(null) }
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")

    fun speakArabic(text: String) {
        if (isTtsReady && tts != null) {
            tts.language = Locale.forLanguageTag("ar")
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    val transitionState = remember { SeekableTransitionState(selectedCategory) }
    LaunchedEffect(selectedCategory) {
        transitionState.animateTo(selectedCategory)
    }

    PredictiveBackHandler(enabled = selectedCategory != null) { progress ->
        try {
            progress.collect { backEvent ->
                transitionState.seekTo(backEvent.progress, targetState = null)
            }
            selectedCategory = null
        } catch (_: CancellationException) {
            transitionState.animateTo(selectedCategory)
        }
    }

    with(sharedTransitionScope) {
        val transition = rememberTransition(transitionState, label = "MemorizationTransition")
        transition.AnimatedContent(
            transitionSpec = {
                if (targetState != null) {
                    (fadeIn(tween(300)) + scaleIn(initialScale = 0.85f, animationSpec = tween(300))) togetherWith (fadeOut(tween(150)) + scaleOut(targetScale = 1.15f, animationSpec = tween(300)))
                } else {
                    (fadeIn(tween(300)) + scaleIn(initialScale = 1.15f, animationSpec = tween(300))) togetherWith (fadeOut(tween(150)) + scaleOut(targetScale = 0.85f, animationSpec = tween(300)))
                }
            }
        ) { targetState ->
            if (targetState == null) {
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedContent) {
                    MemorizationCategorySelection(
                        title = if (lang == AppLanguage.GERMAN) "Auswendig lernen" else "Ezberle",
                        categories = categories,
                        onCategorySelected = { selectedCategory = it }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            rememberSharedContentState(key = "category_${targetState.id}"),
                            animatedVisibilityScope = this@AnimatedContent
                        )
                ) {
                    MemorizationItemsList(
                        category = targetState,
                        onBack = { selectedCategory = null },
                        onSpeak = ::speakArabic
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MemorizationCategorySelection(
    title: String,
    categories: List<MemorizationCategory>,
    onCategorySelected: (MemorizationCategory) -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No AnimatedVisibilityScope found")

    with(sharedTransitionScope) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 160.dp)
            ) {
                items(categories, key = { it.id }) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedBounds(
                                rememberSharedContentState(key = "category_${category.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .clickable { onCategorySelected(category) },
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
                                    Text(
                                        text = category.items.size.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.categoryName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${category.items.size} ${if (category.items.size == 1) "Teil" else "Teile"}",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorizationItemsList(
    category: MemorizationCategory,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val lang = LocalAppLanguage.current
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(
            title = { Text(category.categoryName, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Text(
                text = if (lang == AppLanguage.GERMAN)
                    "Tippe auf eine Karte, um die Aussprache und Übersetzung zu sehen. Nutze den Play-Button für die Audio-Wiedergabe."
                else
                    "Okunuşu ve tercümeyi görmek için bir karta dokunun. Sesli dinlemek için oynat düğmesini kullanın.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(category.items) { item ->
                MemorizationItemCard(item, onSpeak)
            }
        }
    }
}

@Composable
fun MemorizationItemCard(item: MemorizationItem, onSpeak: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val lang = LocalAppLanguage.current
    val hasArabic = item.arabic.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (item.note.isNotEmpty()) {
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .animateContentSize()
                    .clickable { isExpanded = !isExpanded }
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasArabic) {
                            Text(
                                text = item.arabic,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onSpeak(item.arabic) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Text(
                                text = if (lang == AppLanguage.GERMAN) "Anzeigen" else "Gör",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (isExpanded) {
                        if (item.transliteration.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                text = item.transliteration,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (item.translation.isNotEmpty()) {
                            if (item.transliteration.isEmpty()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            } else {
                                Spacer(Modifier.height(4.dp))
                            }
                            Text(
                                text = item.translation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        Text(
                            text = if (lang == AppLanguage.GERMAN) "Details anzeigen..." else "Detayları göster...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
