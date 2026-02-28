package com.example.myapplication.ui

import android.os.Bundle
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import java.util.*

data class MemorizationItem(
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val note: String = ""
)

data class MemorizationCategory(
    val id: String,
    val categoryName: String,
    val items: List<MemorizationItem>
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MemorizationScreen(
    tts: TextToSpeech?,
    isTtsReady: Boolean,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val categories = remember(lang) { getMemorizationData(lang) }
    var selectedCategory by remember { mutableStateOf<MemorizationCategory?>(null) }

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

    SharedTransitionLayout(modifier = modifier) {
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
                MemorizationCategorySelection(
                    title = if (lang == AppLanguage.GERMAN) "Auswendig lernen" else "Ezberle",
                    categories = categories,
                    onCategorySelected = { selectedCategory = it },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            rememberSharedContentState(key = targetState.id),
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
    onCategorySelected: (MemorizationCategory) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
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
                                rememberSharedContentState(key = category.id),
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

fun getMemorizationData(lang: AppLanguage) = when (lang) {
    AppLanguage.GERMAN -> listOf(
        MemorizationCategory(
            id = "surahs",
            categoryName = "Koranische Suren",
            items = listOf(
                MemorizationItem(
                    title = "Sure Al-Fatihah",
                    note = "Pflicht - Wird in jeder Gebetseinheit rezitiert",
                    arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ. الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ. الرَّحْمَٰنِ الرَّحِيمِ. مَالِكِ يَوْمِ الدِّينِ. إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ. اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ. صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّALِّينَ",
                    transliteration = "Bismillahir-rahmanir-rahim. Al-hamdu lillahi rabbil-'alamin. Ar-rahmanir-rahim. Maliki yawmid-din. Iyyaka na'budu wa iyyaka nasta'in. Ihdinas-siratal-mustaqim. Siratal-ladhina an'amta 'alayhim, ghayril-maghdubi 'alayhim walad-dallin.",
                    translation = "Im Namen Allahs, des Gnädigen, des Barmherzigen. Alles Lob gebührt Allah, dem Herrn der Welten. Dem Gnädigen, dem Barmherzigen. Herrscher am Tage des Gerichts. Dir allein dienen wir, und zu Dir allein flehen wir um Hilfe. Führe uns den geraden Weg, den Weg derer, denen Du Gnade erwiesen hast, nicht den Weg derer, die Deinen Zorn erregt haben, und nicht den Weg der Irrenden."
                ),
                MemorizationItem(
                    title = "Sure Al-Kauthar",
                    note = "Kürzeste Sure - Ideal für den Anfang",
                    arabic = "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ. فَصَلِّ لِرَبِّكَ وَانْحَرْ. إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ",
                    transliteration = "Innaa a'taynaakal Kawthar. Fasalli li Rabbika wanhar. Inna shaani'aka huwal abtar.",
                    translation = "Gewiss, Wir haben dir die Fülle gegeben. So bete zu deinem Herrn und opfere. Gewiss, der dich hasst, er ist der Abgeschnittene."
                ),
                MemorizationItem(
                    title = "Sure Al-Ikhlas",
                    note = "Kurze Sure - Wird oft in den ersten beiden Einheiten rezitiert",
                    arabic = "قُلْ هُوَ اللَّهُ أَحَدٌ. اللَّهُ الصَّمَدُ. لَمْ يَلِدْ وَلَمْ يُOLَدْ. وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                    transliteration = "Qul huwal-lahu ahad. Allahus-samad. Lam yalid wa lam yulad. Wa lam yakun lahu kufuwan ahad.",
                    translation = "Sag: Er ist Allah, ein Einziger. Allah, der Überlegene. Er hat nicht gezeugt und ist nicht gezeugt worden. Und niemand ist Ihm jemals gleich."
                )
            )
        ),
        MemorizationCategory(
            id = "movements",
            categoryName = "Bewegungsphrasen",
            items = listOf(
                MemorizationItem(
                    title = "Takbir",
                    note = "Beim Wechsel der Positionen",
                    arabic = "اللَّهُ أَكْبَرُ",
                    transliteration = "Allahu Akbar",
                    translation = "Allah ist am größten."
                ),
                MemorizationItem(
                    title = "Ruku (Verbeugung)",
                    note = "Wird 3 Mal gesagt",
                    arabic = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                    transliteration = "Subhana Rabbiyal-Adhim",
                    translation = "Gepriesen sei mein Herr, der Gewaltige."
                ),
                MemorizationItem(
                    title = "Aufrichten (Sami' Allah)",
                    arabic = "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ",
                    transliteration = "Sami' Allahu liman hamidah",
                    translation = "Allah hört den, der Ihn preist."
                ),
                MemorizationItem(
                    title = "Aufrecht Stehen",
                    arabic = "رَبَّنَا وَلَكَ الْحَمْدُ",
                    transliteration = "Rabbana wa lakal-hamd",
                    translation = "Unser Herr, Dir gebührt alles Lob."
                ),
                MemorizationItem(
                    title = "Sujud (Niederwerfung)",
                    note = "Wird 3 Mal gesagt",
                    arabic = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                    transliteration = "Subhana Rabbiyal-A'la",
                    translation = "Gepriesen sei mein Herr, der Allerhöchste."
                ),
                MemorizationItem(
                    title = "Sitzen zwischen Niederwerfungen",
                    arabic = "رَبِّ اغْفِرْ لِي",
                    transliteration = "Rabbighfir li",
                    translation = "Mein Herr, vergib mir."
                )
            )
        ),
        MemorizationCategory(
            id = "ending",
            categoryName = "Sitzen & Abschluss",
            items = listOf(
                MemorizationItem(
                    title = "Tashahhud",
                    note = "Rezitiert im ersten und letzten Sitzen",
                    arabic = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَAMُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّALِحِينَ، أَشْhَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ، وَأَشْhَدُ அன்َّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                    transliteration = "At-tahiyyatu lillahi was-salawatu wat-tayyibatu, as-salamu 'alayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuhu, as-salamu 'alayna wa 'ala 'ibadillahis-salihin. Ashhadu an la ilaha illallah wa ashhadu anna Muhammadan 'abduhu wa rasuluh.",
                    translation = "Alle Ehrerweisungen, Gebete und die guten Dinge gebühren Allah. Friede sei mit dir, o Prophet... Ich bezeuge, dass es keinen Gott gibt außer Allah, und ich bezeuge, dass Muhammad Sein Diener und Gesandter ist."
                ),
                MemorizationItem(
                    title = "Salawat",
                    note = "Nur im letzten Sitzen rezitiert",
                    arabic = "اللَّhُمَّ صَلِّ عَلَى مُhَمَّدٍ وَعَلَى آلِ مُhَمَّدٍ كَمَا صَلِّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ hَمِيدٌ مَجِIDٌ، اللَّhُمَّ بَارِكْ عَلَى مُhَمَّدٍ وَعَلَى آلِ مُhَمَّدٍ كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ hَمِيدٌ مَجِIDٌ",
                    transliteration = "Allahumma salli 'ala Muhammadin wa 'ala ali Muhammad, kama sallayta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid. Allahumma barik 'ala Muhammadin wa 'ala ali Muhammad, kama barakta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid.",
                    translation = "O Allah, segne Muhammad und die Familie von Muhammad, wie Du Abraham und die Familie von Abraham gesegnet hast... Wahrlich, Du bist der Preiswürdige, der Ruhmreiche."
                ),
                MemorizationItem(
                    title = "Taslim",
                    note = "Abschluss des Gebets",
                    arabic = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                    transliteration = "Assalamu alaykum wa rahmatullah",
                    translation = "Friede sei mit euch und die Barmherzigkeit Allahs."
                )
            )
        ),
        MemorizationCategory(
            id = "structure",
            categoryName = "Gebetsablauf (Struktur)",
            items = listOf(
                MemorizationItem(
                    title = "2-Einheiten Gebet (z.B. Fajr)",
                    note = "Vollständiger Ablauf",
                    arabic = "", transliteration = "",
                    translation = """
                        Vorbereitung: Absicht im Herzen fassen und zur Qibla (Mekka) wenden.
                        
                        Einheit 1:
                        1. Takbeer: Allahu Akbar sagen.
                        2. Stehen (Qiyam): Al-Fatihah + kurze Sure rezitieren.
                        3. Verbeugung (Ruku): 3x Subhana Rabbiyal-Adhim.
                        4. Aufrichten: Sami' Allahu... dann Rabbana wa lakal-hamd.
                        5. Sujud: 3x Subhana Rabbiyal-A'la.
                        6. Sitzen: Rabbighfir li.
                        7. Sujud: 3x Subhana Rabbiyal-A'la.
                        
                        Einheit 2:
                        8. Aufstehen für die zweite Einheit.
                        9. Stehen (Qiyam): Al-Fatihah + kurze Sure rezitieren.
                        10. Alle Bewegungen (Ruku bis Sujud) wiederholen.
                        
                        Abschluss:
                        11. Letztes Sitzen: Tashahhud & Salawat rezitieren.
                        12. Tasleem: Kopf nach rechts, dann nach links drehen und grüßen.
                    """.trimIndent()
                ),
                MemorizationItem(
                    title = "3-Einheiten Gebet (z.B. Maghrib)",
                    note = "Struktureller Ablauf",
                    arabic = "", transliteration = "",
                    translation = """
                        Einheit 1 & 2: Wie beim 2-Einheiten Gebet.
                        Nach Einheit 2: Erstes Sitzen - Nur Tashahhud rezitieren (kein Salawat).
                        
                        Einheit 3:
                        1. Aufstehen.
                        2. Nur Al-Fatihah rezitieren (keine zusätzliche Sure).
                        3. Alle Bewegungen (Ruku bis Sujud) ausführen.
                        
                        Abschluss:
                        1. Letztes Sitzen: Vollständiger Tashahhud & Salawat.
                        2. Tasleem: Das Gebet beenden.
                    """.trimIndent()
                ),
                MemorizationItem(
                    title = "4-Einheiten Gebet (Dhuhr, Asr, Isha)",
                    note = "Struktureller Ablauf",
                    arabic = "", transliteration = "",
                    translation = """
                        Einheit 1 & 2: Wie gewohnt (mit Al-Fatihah + Sure).
                        Nach Einheit 2: Erstes Sitzen - Nur Tashahhud rezitieren.
                        
                        Einheit 3 & 4:
                        1. Aufstehen.
                        2. Nur Al-Fatihah rezitieren (keine zusätzliche Sure).
                        3. Alle Bewegungen (Ruku bis Sujud) ausführen.
                        
                        Abschluss:
                        1. Letztes Sitzen: Vollständiger Tashahhud & Salawat.
                        2. Tasleem: Das Gebet beenden.
                    """.trimIndent()
                )
            )
        )
    )
    AppLanguage.TURKISH -> listOf(
        MemorizationCategory(
            id = "surahs",
            categoryName = "Kur'an Sureleri",
            items = listOf(
                MemorizationItem(
                    title = "Fatiha Suresi",
                    note = "Farz - Her rekatta okunur",
                    arabic = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ. الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ. الرَّحْمَنِ الرَّحِيمِ. مَالِكِ يَوْMِ الدِّينِ. إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ. اهْدِنَا الصِّرَاطَ الْمُسْتAKİM. SIRÂTALLEZÎNE EN'AMTE ALEYHİM GAYRİLMAĞDÛBİ ALEYHİM VELEDDÂLLÎN.",
                    transliteration = "Bismillâhirrahmânirrahîm. Elhamdü lillâhi rabbil'alemin. Errahmânirrahîm. Mâliki yevmiddîn. İyyâke na'büdü ve iyyâke neste'în. İhdinessırâtel müstakîm. Sırâtallezîne en'amte aleyhim gayrilmağdûbi aleyhim veleddâllîn.",
                    translation = "Rahman ve Rahim olan Allah'ın adıyla. Hamd, Alemlerin Rabbi Allah'a mahsustur. O, Rahman ve Rahimdir. Hesap ve ceza gününün malikidir. Yalnız Sana ibadet eder ve yalnız Senden yardım dileriz. Bizi doğru yola ilet; kendilerine nimet verdiklerinin yoluna, gazaba uğrayanlarınkine ve sapkınlarınkine değil."
                ),
                MemorizationItem(
                    title = "Kevser Suresi",
                    note = "En kısa sure - Başlangıç için ideal",
                    arabic = "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ. فَصَلِّ لِرَبِّكَ وَانْحَرْ. إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ",
                    transliteration = "İnnâ a'taynâkel kevser. Fesalli lirabbike venhar. İnne şânieke hüvel ebter.",
                    translation = "Şüphesiz biz sana Kevser'i verdik. Öyleyse Rabbin için namaz kıl ve kurban kes. Asıl sonu kesik olan, şüphesiz sana hınç besleyendir."
                ),
                MemorizationItem(
                    title = "İhlas Suresi",
                    note = "Kısa Sure - Genellikle ilk iki rekatta okunur",
                    arabic = "قُلْ هُوَ اللَّهُ أَحَدٌ. اللَّهُ الصَّمَدُ. لَمْ يَلِدْ وَلَم_ ي_و_ل_d_ْ. وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                    transliteration = "Kul hüvellâhü ehad. Allâhüssamed. Lem yelid ve len yûled. Ve len yekün lehû küfüven ehad.",
                    translation = "De ki: O Allah birdir. Allah Samed'dir. O, doğurmamış ve doğurulmamıştır. Onun hiçbir dengi yoktur."
                )
            )
        ),
        MemorizationCategory(
            id = "movements",
            categoryName = "Hareket Duaları",
            items = listOf(
                MemorizationItem(
                    title = "Tekbir",
                    note = "Pozisyon değiştirirken",
                    arabic = "اللَّهُ أَكْبَرُ",
                    transliteration = "Allahu Ekber",
                    translation = "Allah en büyüktür."
                ),
                MemorizationItem(
                    title = "Rüku",
                    note = "3 kez söylenir",
                    arabic = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                    transliteration = "Sübhâne Rabbiye’l-Azîm",
                    translation = "Azamet sahibi olan Rabbim her türlü noksanlıktan münezzehtir."
                ),
                MemorizationItem(
                    title = "Doğrulmak (Semi' Allah)",
                    arabic = "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ",
                    transliteration = "Semi’allâhu limen hamideh",
                    translation = "Allah hamd edeni işitti."
                ),
                MemorizationItem(
                    title = "Ayakta Durmak",
                    arabic = "رَبَّنَا وَلَكَ الْحَمْدُ",
                    transliteration = "Rabbenâ leke’l-hamd",
                    translation = "Rabbimiz, hamd sana mahsustur."
                ),
                MemorizationItem(
                    title = "Secde",
                    note = "3 kez söylenir",
                    arabic = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                    transliteration = "Sübhâne Rabbiye’l-A’lâ",
                    translation = "Yüce olan Rabbim her türlü noksan sıfatlardan münezzehtir."
                ),
                MemorizationItem(
                    title = "İki Secde Arası Oturuş",
                    arabic = "رَبِّ اغْفِرْ لِي",
                    transliteration = "Rabbighfir li",
                    translation = "Rabbim beni bağışla."
                )
            )
        ),
        MemorizationCategory(
            id = "ending",
            categoryName = "Oturuş ve Selam",
            items = listOf(
                MemorizationItem(
                    title = "Tahiyyat",
                    note = "İlk ve son oturuşta okunur",
                    arabic = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّALِحِينَ، أَشْhَدُ أَنْ لَا إِلَٰهَ إِلَّا اللَّهُ، وَأَشْhَدُ அன்َّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                    transliteration = "Ettehıyyâtü lillâhi vessalevâtü vettayyıbât. Esselâmü aleyke eyyühen-nebiyyü ve rahmetüllâhi ve berekâtüh. Esselâmü aleynâ ve alâ ibâdillâhis-salihîn. Eşhedü en lâ ilâhe illallâh ve eşhedü enne Muhammeden abdühû ve rasûlüh.",
                    translation = "Bütün dualar, senalar, bedeni ve mali ibadetler Allah'a mahsustur... Şahitlik ederim ki Allah'tan başka ilah yoktur. Yine shahitlik ederim ki Muhammed O'nun kulu und elçisidir."
                ),
                MemorizationItem(
                    title = "Salli-Barik",
                    note = "Sadece son oturuşta okunur",
                    arabic = "اللَّhُمَّ صَلِّ عَلَى مُhَمَّدٍ وَعَلَى آلِ مُhَمَّدٍ كَمَا صَلِّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ hَمِيدٌ مَGِIDٌ، اللَّhُمَّ بَارِكْ عَلَى مُhَمَّدٍ وَعَلَى آلِ مُhَمَّدٍ كَمَا bârekte alâ İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd.",
                    transliteration = "Allâhümme salli alâ Muhammedin... Kemâ salleyte alâ İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd. Allâhümme bârik alâ Muhammedin... Kemâ bârekte alâ İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd.",
                    translation = "Allah'ım! Hz. Muhammed'e ve onun aline salat et... Şüphesek Sen, övülmeye layık und sherefi yüce olansın."
                ),
                MemorizationItem(
                    title = "Selam",
                    note = "Namazın sonunda",
                    arabic = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                    transliteration = "Esselâmu aleyküm ve rahmetullâh",
                    translation = "Allah'ın selamı ve rahmeti üzerinize olsun."
                )
            )
        ),
        MemorizationCategory(
            id = "structure",
            categoryName = "Namazın Kılınışı (Yapısı)",
            items = listOf(
                MemorizationItem(
                    title = "2 Rekatlık Namaz (Örn. Sabah)",
                    note = "Tam Uygulama",
                    arabic = "", transliteration = "",
                    translation = """
                        Hazırlık: Kalben niyet edin ve Kıbleye (Kabe) yönelin.
                        
                        1. Rekat:
                        1. İftitah Tekbiri: Allahu Ekber diyerek başlayın.
                        2. Kıyam: Fatiha + zammı sure (örn. Kevser) okuyun.
                        3. Rüku: 3x Sübhane Rabbiye'l-Azim.
                        4. Doğrulmak: Semi' Allahu... sonra Rabbenâ leke'l-hamd.
                        5. Secde: 3x Sübhane Rabbiye'l-Alâ.
                        6. Oturuş: Rabbighfir li.
                        7. İkinci Secde.
                        
                        2. Rekat:
                        8. İkinci rekat için ayağa kalkın.
                        9. Kıyam: Fatiha + zammı sure okuyun.
                        10. Tüm hareketleri (Rüku - Secde) tekrarlayın.
                        
                        Bitiş:
                        11. Son Oturuş: Tahiyyat & Salli-Barik dualarını okuyun.
                        12. Selam: Önce sağa sonra sola selam verin.
                    """.trimIndent()
                ),
                MemorizationItem(
                    title = "3 Rekatlık Namaz (Örn. Akşam)",
                    note = "Yapısal Akış",
                    arabic = "", transliteration = "",
                    translation = """
                        1. ve 2. Rekat: 2 rekatlık namaz gibi kılınır.
                        2. Rekattan sonra: Ara oturuş - Sadece Tahiyyat okunur (Salli-Barik okunmaz).
                        
                        3. Rekat:
                        1. Ayağa kalkın.
                        2. Sadece Fatiha okuyun (Zammı sure eklenmez).
                        3. Tüm hareketleri (Rüku - Secde) yapın.
                        
                        Bitiş:
                        1. Son Oturuş: Tam Tahiyyat & Salli-Barik.
                        2. Selam: Namazı bitirin.
                    """.trimIndent()
                ),
                MemorizationItem(
                    title = "4 Rekatlık Namaz (Öğle, İkindi, Yatsı)",
                    note = "Yapısal Akış",
                    arabic = "", transliteration = "",
                    translation = """
                        1. ve 2. Rekat: Normal şekilde (Fatiha + Sure).
                        2. Rekattan sonra: Ara oturuş - Sadece Tahiyyat okunur.
                        
                        3. ve 4. Rekat:
                        1. Ayağa kalkın.
                        2. Sadece Fatiha okuyun (Zammı sure eklenmez).
                        3. Tüm hareketleri (Rüku - Secde) yapın.
                        
                        Bitiş:
                        1. Son Oturuş: Tam Tahiyyat & Salli-Barik.
                        2. Selam: Namazı bitirin.
                    """.trimIndent()
                )
            )
        )
    )
}
