package com.example.myapplication

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.AppLanguage
import com.example.myapplication.ui.LocalAppLanguage
import com.example.myapplication.ui.PrayerLearnScreen
import com.example.myapplication.ui.PrayerReferenceScreen
import com.example.myapplication.ui.PrerequisitesScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PastelLavender
import com.example.myapplication.ui.theme.PastelPeach
import com.example.myapplication.ui.theme.PastelSkyBlue
import kotlinx.coroutines.CancellationException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentLanguage by rememberSaveable { mutableStateOf(AppLanguage.GERMAN) }
            
            CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
                MyApplicationTheme {
                    MyApplicationApp(
                        onLanguageChange = { currentLanguage = it }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationApp(onLanguageChange: (AppLanguage) -> Unit) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.SCHRITT_FUER_SCHRITT) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLanguage = LocalAppLanguage.current
    val context = LocalContext.current

    // Initialize TTS once at the top level
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val ttsProvider = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        tts = ttsProvider
        onDispose {
            ttsProvider.stop()
            ttsProvider.shutdown()
        }
    }

    // Correct implementation of PredictiveBackHandler
    PredictiveBackHandler(enabled = currentDestination != AppDestinations.SCHRITT_FUER_SCHRITT) { progress ->
        try {
            progress.collect { /* Optional: handle progress for custom animation */ }
            currentDestination = AppDestinations.SCHRITT_FUER_SCHRITT
        } catch (_: CancellationException) {
            // Handle cancellation if needed
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(if (currentLanguage == AppLanguage.GERMAN) "Sprache wählen" else "Dil Seçin") },
            text = {
                Column {
                    AppLanguage.entries.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChange(lang)
                                    showLanguageDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = lang == currentLanguage, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(lang.label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(if (currentLanguage == AppLanguage.GERMAN) "Schließen" else "Kapat")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Highly visible Floating Pill Navigation Bar with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        PastelLavender,
                                        PastelSkyBlue,
                                        PastelPeach
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    ) {
                        AppDestinations.entries.forEach { destination ->
                            val isSelected = destination == currentDestination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        destination.icon,
                                        contentDescription = if (currentLanguage == AppLanguage.GERMAN) destination.labelDe else destination.labelTr
                                    )
                                },
                                label = { 
                                    Text(
                                        text = if (currentLanguage == AppLanguage.GERMAN) destination.labelDe else destination.labelTr,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF6A5ACD), // SlateBlue for better contrast on pastel
                                    selectedTextColor = Color(0xFF6A5ACD),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLanguageDialog = true },
                containerColor = PastelSkyBlue,
                contentColor = Color(0xFF6A5ACD),
                modifier = Modifier.padding(bottom = 100.dp) // Move up to avoid overlap with pill nav
            ) {
                Icon(Icons.Default.Translate, contentDescription = "Language")
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentDestination,
            modifier = Modifier.padding(innerPadding),
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal)
                    AnimatedContentTransitionScope.SlideDirection.Left
                else
                    AnimatedContentTransitionScope.SlideDirection.Right

                slideIntoContainer(
                    towards = direction,
                    animationSpec = tween(400)
                ) togetherWith slideOutOfContainer(
                    towards = direction,
                    animationSpec = tween(400)
                )
            },
            label = "TabTransition"
        ) { destination ->
            Surface(modifier = Modifier.fillMaxSize()) {
                when (destination) {
                    AppDestinations.MITBETEN -> PrayerLearnScreen(
                        tts = tts,
                        isTtsReady = isTtsReady,
                        modifier = Modifier.fillMaxSize()
                    )
                    AppDestinations.SCHRITT_FUER_SCHRITT -> PrayerReferenceScreen(
                        tts = tts,
                        isTtsReady = isTtsReady,
                        onStartPray = { currentDestination = AppDestinations.MITBETEN },
                        modifier = Modifier.fillMaxSize()
                    )
                    AppDestinations.VORAUSSETZUNGEN -> PrerequisitesScreen(Modifier.fillMaxSize())
                }
            }
        }
    }
}

enum class AppDestinations(
    val labelDe: String,
    val labelTr: String,
    val icon: ImageVector,
) {
    MITBETEN("Mitbeten", "Eşlik Et", Icons.Default.PlayArrow),
    SCHRITT_FUER_SCHRITT("Schritt für Schritt", "Adım Adım", Icons.AutoMirrored.Filled.List),
    VORAUSSETZUNGEN("Voraussetzungen", "Ön Şartlar", Icons.Default.CheckCircle),
}
