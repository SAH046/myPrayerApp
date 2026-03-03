package com.example.myapplication.ui

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import kotlin.coroutines.resume

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PrayerLearnScreen(
    tts: TextToSpeech?,
    isTtsReady: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedPrayer by remember { mutableStateOf<PrayerConfig?>(null) }
    val isInspectionMode = LocalInspectionMode.current
    val lang = LocalAppLanguage.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")

    // Optimize list allocation
    val allPrayers = remember(lang) { getAllPrayers(lang) }

    suspend fun speakSuspend(text: String, locale: Locale) {
        if (isTtsReady && !isInspectionMode) {
            val ttsInstance = tts ?: return
            ttsInstance.stop()
            val cleanText = text.replace(Regex("\\[.*?\\]"), "")
            withTimeoutOrNull(60000L) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val utteranceId = UUID.randomUUID().toString()
                    var resumed = false
                    ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) {}
                        override fun onDone(id: String?) { if (id == utteranceId && !resumed) { resumed = true; continuation.resume(Unit) } }
                        @Deprecated("Deprecated in Java")
                        override fun onError(id: String?) { if (id == utteranceId && !resumed) { resumed = true; continuation.resume(Unit) } }
                        override fun onError(id: String?, errorCode: Int) { if (id == utteranceId && !resumed) { resumed = true; continuation.resume(Unit) } }
                    })
                    ttsInstance.language = locale
                    ttsInstance.speak(cleanText, TextToSpeech.QUEUE_FLUSH, Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId) }, utteranceId)

                    continuation.invokeOnCancellation {
                        ttsInstance.stop()
                    }
                }
            }
        } else if (isInspectionMode) { delay(3000L) }
    }

    val transitionState = remember { SeekableTransitionState(selectedPrayer) }
    LaunchedEffect(selectedPrayer) {
        transitionState.animateTo(selectedPrayer)
    }

    PredictiveBackHandler(enabled = selectedPrayer != null) { progress ->
        try {
            progress.collect { backEvent ->
                transitionState.seekTo(backEvent.progress, targetState = null)
            }
            selectedPrayer = null
        } catch (_: CancellationException) {
            transitionState.animateTo(selectedPrayer)
        }
    }

    with(sharedTransitionScope) {
        val transition = rememberTransition(transitionState, label = "PrayerSelectionTransition")
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
                    PrayerSelectionScreen(
                        title = if (lang == AppLanguage.GERMAN) "Automatisches Mitbeten" else "Otomatik Takip",
                        prayers = allPrayers,
                        onPrayerSelected = { selectedPrayer = it }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            rememberSharedContentState(key = "prayer_${targetState.id}"),
                            animatedVisibilityScope = this@AnimatedContent
                        )
                ) {
                    GuidedPrayerSession(
                        prayerName = targetState.name,
                        rakatCount = targetState.rakat,
                        steps = targetState.steps,
                        onSpeakSuspend = ::speakSuspend,
                        onExit = { selectedPrayer = null }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PrayerReferenceScreen(
    tts: TextToSpeech?,
    isTtsReady: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedPrayer by remember { mutableStateOf<PrayerConfig?>(null) }
    val isInspectionMode = LocalInspectionMode.current
    val lang = LocalAppLanguage.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")

    // Optimize list allocation
    val variants = remember(lang) { getRakatVariants(lang) }

    fun speakArabic(text: String, repeatCount: Int = 1) {
        if (isTtsReady && !isInspectionMode) {
            val ttsInstance = tts ?: return
            ttsInstance.language = Locale.forLanguageTag("ar")
            repeat(repeatCount) {
                ttsInstance.speak(text, TextToSpeech.QUEUE_ADD, null, null)
            }
        }
    }

    val transitionState = remember { SeekableTransitionState(selectedPrayer) }
    LaunchedEffect(selectedPrayer) {
        transitionState.animateTo(selectedPrayer)
    }

    PredictiveBackHandler(enabled = selectedPrayer != null) { progress ->
        try {
            progress.collect { backEvent ->
                transitionState.seekTo(backEvent.progress, targetState = null)
            }
            selectedPrayer = null
        } catch (_: CancellationException) {
            transitionState.animateTo(selectedPrayer)
        }
    }

    with(sharedTransitionScope) {
        val transition = rememberTransition(transitionState, label = "RakatSelectionTransition")
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
                    PrayerSelectionScreen(
                        title = if (lang == AppLanguage.GERMAN) "Schritt für Schritt" else "Adım Adım",
                        prayers = variants,
                        onPrayerSelected = { selectedPrayer = it }
                    )
                }
            } else {
                val condensedSteps = remember(targetState, lang) {
                    val rCount = targetState.rakat
                    val qiyamStep = CommonSteps.rakatSteps(1, lang)[0]
                    listOf(
                        CommonSteps.takbir(lang).copy(title = if (lang == AppLanguage.GERMAN) "Beginn (Takbir)" else "Başlangıç (Tekbir)"),
                        PrayerStep(
                            title = if (lang == AppLanguage.GERMAN) "Die ersten zwei Rakat" else "İlk İki Rekat",
                            posture = if (lang == AppLanguage.GERMAN) "Qiyam" else "Kıyam",
                            description = if (lang == AppLanguage.GERMAN) "Aufrechtes Stehen: Hände zwischen Brust und Nabel verschränkt, rechte Hand auf linken Unterarm legen. Hier rezitieren Sie die al-Fatiha und eine weitere Sure Ihrer Wahl." else "Burada Fatiha suresini ve seçtiğiniz bir sureyi okuyun.",
                            arabicText = qiyamStep.arabicText,
                            transliteration = qiyamStep.transliteration,
                            translation = qiyamStep.translation
                        ),
                        CommonSteps.rakatSteps(1, lang)[1].copy(title = if (lang == AppLanguage.GERMAN) "Die Verbeugung" else "Rüku", posture = if (lang == AppLanguage.GERMAN) "Ruku‘" else "Rüku", description = if (lang == AppLanguage.GERMAN) "Verbeugen: Oberkörper nach vorn beugen, Hände auf den Kniescheiben ablegen. Sagen Sie dreimal die Lobpreisung." else "Eğilin ve üç kez tesbihatı söyleyin."),
                        CommonSteps.rakatSteps(1, lang)[2].copy(title = if (lang == AppLanguage.GERMAN) "Das Aufrichten" else "Doğrulmak", posture = if (lang == AppLanguage.GERMAN) "Qama" else "Kıyam"),
                        CommonSteps.rakatSteps(1, lang)[3].copy(title = if (lang == AppLanguage.GERMAN) "Die Niederwerfung" else "Secde", posture = if (lang == AppLanguage.GERMAN) "Sadschda" else "Secde", description = if (lang == AppLanguage.GERMAN) "Niederwerfung: Stirn, Nase, Handflächen, Knie und Zehenspitzen berühren den Boden. Sagen Sie dreimal die Lobpreisung. Dies geschieht zweimal pro Rakat." else "Üç kez tesbihatı söyleyin. Bu her rekatta iki kez yapılır."),
                        CommonSteps.rakatSteps(1, lang)[4].copy(title = if (lang == AppLanguage.GERMAN) "Das Sitzen" else "Oturuş", posture = if (lang == AppLanguage.GERMAN) "Dschalsa" else "Celse", description = if (lang == AppLanguage.GERMAN) "Aufsitzen: Auf Knien und Füßen sitzen, Hände auf den Knien. Das kurze Sitzen zwischen den beiden Niederwerfungen." else "İki secde arasındaki kısa oturuş."),
                        if (rCount > 2) CommonSteps.middleTashahhud(lang).copy(title = if (lang == AppLanguage.GERMAN) "Mittlerer Tashahhud" else "Ara Oturuş (Tahiyyat)", description = if (lang == AppLanguage.GERMAN) "Aufsitzen: Auf Knien und Füßen sitzen, Hände auf den Knien. Wird nach der 2. Rakat rezitiert (bei 3 oder 4 Rakat Gebeten)." else "2. rekattan sonra okunur (3 veya 4 rekatlı namazlarda).") else null,
                        if (rCount > 2) PrayerStep(
                            title = if (lang == AppLanguage.GERMAN) "Die hinteren Rakat (3. & 4.)" else "Sonraki Rekatlar (3. ve 4.)",
                            posture = if (lang == AppLanguage.GERMAN) "Qiyam" else "Kıyam",
                            description = if (lang == AppLanguage.GERMAN) "Aufrechtes Stehen: Hände zwischen Brust und Nabel verschränkt, rechte Hand auf linken Unterarm legen. In der 3. und 4. Rakat wird nur die al-Fatiha leise rezitiert." else "3. ve 4. rekatta sadece Fatiha suresi sessizce okunur.",
                            arabicText = qiyamStep.arabicText,
                            transliteration = qiyamStep.transliteration,
                            translation = qiyamStep.translation
                        ) else null,
                        CommonSteps.finalTashahhud(lang).copy(title = if (lang == AppLanguage.GERMAN) "Abschluss-Tashahhud" else "Son Oturuş (Tahiyyat)", description = if (lang == AppLanguage.GERMAN) "Aufsitzen: Auf Knien und Füßen sitzen, Hände auf den Knien. Der vollständige Tashahhud am Ende des Gebets." else "Namazın sonundaki tam Tahiyyat."),
                        CommonSteps.finalTaslim(lang).copy(title = if (lang == AppLanguage.GERMAN) "Der Friedensgruß (Taslim)" else "Selam")
                    ).filterNotNull()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            rememberSharedContentState(key = "prayer_${targetState.id}"),
                            animatedVisibilityScope = this@AnimatedContent
                        )
                ) {
                    PrayerStepsListScreen(
                        title = targetState.name,
                        steps = condensedSteps,
                        onBack = { selectedPrayer = null },
                        onSpeak = ::speakArabic
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerStepsListScreen(
    title: String,
    steps: List<PrayerStep>,
    onBack: () -> Unit,
    onSpeak: (String, Int) -> Unit
) {
    val lang = LocalAppLanguage.current
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(
            title = { Text(title, fontWeight = FontWeight.Bold) },
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
                if (lang == AppLanguage.GERMAN) "Diese Liste zeigt die verschiedenen Bestandteile des Gebets ohne Wiederholungen der Rakat-Blöcke." else "Bu liste, rekat tekrarları olmaksızın namazın different bölümlerini göstermektedir.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 160.dp), // Increased bottom padding
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(steps) { index, step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (index + 1).toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, start = 40.dp)
                        )

                        if (step.arabicText.isNotEmpty()) {
                            var isExpanded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 40.dp)
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
                                        Text(
                                            text = step.arabicText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { onSpeak(step.arabicText, step.repeatCount) }) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    if (isExpanded) {
                                        if (step.transliteration.isNotEmpty()) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                            Text(
                                                text = step.transliteration,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontStyle = FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (step.translation.isNotEmpty()) {
                                            if (step.transliteration.isEmpty()) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    color = MaterialTheme.colorScheme.outlineVariant
                                                )
                                            } else {
                                                Spacer(Modifier.height(4.dp))
                                            }
                                            Text(
                                                text = step.translation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = if (lang == AppLanguage.GERMAN) "Details (Aussprache & Übersetzung) anzeigen..." else "Detayları (Okunuş & Tercüme) göster...",
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
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PrayerSelectionScreen(
    title: String,
    prayers: List<PrayerConfig>,
    onPrayerSelected: (PrayerConfig) -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No AnimatedVisibilityScope found")

    with(sharedTransitionScope) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp, top = 16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 160.dp) // Increased bottom padding
            ) {
                items(prayers, key = { it.id }) { prayer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedBounds(
                                rememberSharedContentState(key = "prayer_${prayer.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .clickable { onPrayerSelected(prayer) },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(prayer.rakat.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prayer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(prayer.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuidedPrayerSession(
    prayerName: String,
    rakatCount: Int,
    steps: List<PrayerStep>,
    onSpeakSuspend: suspend (String, Locale) -> Unit,
    onExit: () -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(-1) }
    var countdownValue by remember { mutableIntStateOf(5) }
    val lang = LocalAppLanguage.current

    LaunchedEffect(Unit) {
        while (countdownValue > 0) { delay(1000L); countdownValue-- }
        currentStepIndex = 0
    }

    LaunchedEffect(currentStepIndex) {
        if (currentStepIndex >= 0 && currentStepIndex < steps.size) {
            val step = steps[currentStepIndex]
            onSpeakSuspend(step.description, lang.locale)
            delay(1000L)
            if (step.arabicText.isNotEmpty()) {
                repeat(step.repeatCount) {
                    onSpeakSuspend(step.arabicText, Locale.forLanguageTag("ar"))
                    if (it < step.repeatCount - 1) delay(1000L)
                }
                delay(1500L)
            } else {
                delay(3000L)
            }
            if (currentStepIndex < steps.size - 1) currentStepIndex++ else onExit()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (currentStepIndex == -1) {
            CountdownUI(prayerName, rakatCount, countdownValue)
        } else if (currentStepIndex < steps.size) {
            ActiveStepUI(prayerName, rakatCount, steps[currentStepIndex], currentStepIndex, steps.size, onExit)
        }
    }
}

@Composable
fun CountdownUI(prayerName: String, rakatCount: Int, value: Int) {
    val lang = LocalAppLanguage.current
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            text = "$prayerName ($rakatCount Rakat)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
        Text(if (lang == AppLanguage.GERMAN) "Bereit machen..." else "Hazırlanın...", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(if (lang == AppLanguage.GERMAN) "Lege dein Handy vor dich hin." else "Telefonunuzu önünüze koyun.", textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(Modifier.height(48.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
            Text(text = value.toString(), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActiveStepUI(prayerName: String, rakatCount: Int, step: PrayerStep, index: Int, total: Int, onExit: () -> Unit) {
    val lang = LocalAppLanguage.current
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Show Prayer Name and Rakat
        Text(
            text = "$prayerName ($rakatCount Rakat)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (lang == AppLanguage.GERMAN) "Schritt ${index + 1} von $total" else "${index + 1}. Adım / $total", style = MaterialTheme.typography.labelLarge)
            IconButton(onClick = onExit) { Icon(Icons.Default.Close, contentDescription = if (lang == AppLanguage.GERMAN) "Beenden" else "Bitir") }
        }
        LinearProgressIndicator(progress = { (index + 1).toFloat() / total }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp) )
        Spacer(Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = step.posture, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    if (step.images.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            step.images.forEach { Image(painter = painterResource(it), contentDescription = null, modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp), contentScale = ContentScale.Fit) }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                if (step.arabicText.isNotEmpty()) {
                    Text(text = step.arabicText, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    if (step.transliteration.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(text = step.transliteration, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    Text(text = step.description, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(160.dp)) // Increased space inside the scrollable card
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrayerGuidePreview() {
    MyApplicationTheme {
        PrayerLearnScreen(tts = null, isTtsReady = false)
    }
}
