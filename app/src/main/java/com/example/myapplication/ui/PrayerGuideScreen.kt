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
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import kotlin.coroutines.resume

enum class AppLanguage(val label: String, val locale: Locale) {
    GERMAN("Deutsch", Locale.GERMAN),
    TURKISH("Türkçe", Locale.forLanguageTag("tr-TR"))
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.GERMAN }

data class PrayerStep(
    val title: String,
    val posture: String,
    val description: String,
    val arabicText: String = "",
    val transliteration: String = "",
    val translation: String = "",
    val images: List<Int> = emptyList(),
    val durationMillis: Long = 5000L,
    val repeatCount: Int = 1
)

data class PrayerConfig(
    val id: String,
    val name: String,
    val description: String,
    val rakat: Int,
    val steps: List<PrayerStep>
)

object CommonSteps {
    fun niyyah(prayerName: String, rakat: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.GERMAN -> PrayerStep(
            title = "Niyyah (Absicht)",
            posture = "Qiyam (Stehen)",
            description = "Fassen Sie die Absicht für das $prayerName Gebet ($rakat Rakat) fest in Ihrem Herzen.",
            images = listOf(R.drawable.niyyah),
            durationMillis = 6000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "Niyet",
            posture = "Kıyam (Ayakta durmak)",
            description = "$prayerName namazı için ($rakat rekat) niyet edin.",
            images = listOf(R.drawable.niyyah),
            durationMillis = 6000L
        )
    }

    fun takbir(lang: AppLanguage) = when (lang) {
        AppLanguage.GERMAN -> PrayerStep(
            title = "Takbirat al-Ihram",
            posture = "Hände heben",
            description = "Heben Sie die Hände und sagen Sie einmalig:",
            arabicText = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translation = "Allah ist am größten",
            images = listOf(R.drawable.takbirat),
            durationMillis = 4000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "İftitah Tekbiri",
            posture = "Elleri kaldırmak",
            description = "Ellerinizi kaldırın ve bir kez söyleyin:",
            arabicText = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Ekber",
            translation = "Allah en büyüktür",
            images = listOf(R.drawable.takbirat),
            durationMillis = 4000L
        )
    }

    fun rakatSteps(number: Int, lang: AppLanguage, withExtraSura: Boolean = true) = listOf(
        when (lang) {
            AppLanguage.GERMAN -> PrayerStep(
                title = "Rakat $number: Qiyam",
                posture = "Stehen",
                description = "Rezitieren Sie die al-Fatiha" + if (withExtraSura) " und eine weitere Sure." else ".",
                arabicText = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ. الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ. الرَّحْمَنِ الرَّحِيمِ. مَالِكِ يَوْمِ الدِّينِ. إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ. اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ. صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                transliteration = "Bismillahir-rahmanir-rahim. Al-hamdu lillahi rabbil-'alamin. Ar-rahmanir-rahim. Maliki yawmid-din. Iyyaka na'budu wa iyyaka nasta'in. Ihdinas-siratal-mustaqim. Siratal-ladhina an'amta 'alayhim, ghayril-maghdubi 'alayhim walad-dallin.",
                translation = "Im Namen Allahs, des Gnädigen, des Barmherzigen. Alles Lob gebührt Allah, dem Herrn der Welten. Dem Gnädigen, dem Barmherzigen. Herrscher am Tage des Gerichts. Dir allein dienen wir, und zu Dir allein flehen wir um Hilfe. Führe uns den geraden Weg, den Weg derer, denen Du Gnade erwiesen hast, nicht den Weg derer, die Deinen Zorn erregt haben, und nicht den Weg der Irrenden.",
                images = listOf(R.drawable.qiyam),
                durationMillis = 20000L
            )
            AppLanguage.TURKISH -> PrayerStep(
                title = "$number. Rekat: Kıyam",
                posture = "Ayakta durmak",
                description = "Fatiha suresini" + if (withExtraSura) " ve ek bir sure okuyun." else " okuyun.",
                arabicText = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ. الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ. الرَّحْمَن. الرَّحِيمِ. مَالِكِ يَوْمِ الدِّينِ. إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ. اهْدِنَا الصِّİREKAL MÜSTAKİM. SIRÂTALLEZÎNE EN'AMTE ALEYHİM GAYRİLMAĞDÛBİ ALEYHİM VELEDDÂLLÎN.",
                transliteration = "Bismillâhirrahmânirrahîm. Elhamdü lillâhi rabbil'alemin. Errahmânirrahîm. Mâliki yevmiddîn. İyyâke na'büdü ve iyyâke neste'în. İhdinessırâtel müstakîm. Sırâtallezîne en'amte aleyhim gayrilmağdûbi aleyhim veleddâllîn.",
                translation = "Rahman ve Rahim olan Allah'ın adıyla. Hamd, Alemlerin Rabbi Allah'a mahsustur. O, Rahman ve Rahimdir. Hesap ve ceza gününün malikidir. Yalnız Sana ibadet eder ve yalnız Senden yardım dileriz. Bizi doğru yola ilet; kendilerine nimet verdiklerinin yoluna, gazaba uğrayanlarınkine ve sapkınlarınkine değil.",
                images = listOf(R.drawable.qiyam),
                durationMillis = 20000L
            )
        },
        when (lang) {
            AppLanguage.GERMAN -> PrayerStep(
                title = "Rakat $number: Ruku",
                posture = "Verbeugung",
                description = "Beugen Sie sich vor und sagen Sie dreimal:",
                arabicText = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                transliteration = "Subhana Rabbiyal-Adhim",
                translation = "Gepriesen sei mein Herr, der Gewaltige",
                images = listOf(R.drawable.ruku),
                repeatCount = 3,
                durationMillis = 8000L
            )
            AppLanguage.TURKISH -> PrayerStep(
                title = "$number. Rekat: Rüku",
                posture = "Eğilmek",
                description = "Eğilin ve üç kez söyleyin:",
                arabicText = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                transliteration = "Sübhâne Rabbiye’l-Azîm",
                translation = "Azamet sahibi olan Rabbim her türlü noksanlıktan münezzehtir",
                images = listOf(R.drawable.ruku),
                repeatCount = 3,
                durationMillis = 8000L
            )
        },
        when (lang) {
            AppLanguage.GERMAN -> PrayerStep(
                title = "Rakat $number: Aufrichten",
                posture = "Stehen",
                description = "Richten Sie sich auf und sagen Sie:",
                arabicText = "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ. رَبَّنَا وَلَكَ الْحَمْدُ",
                transliteration = "Sami'Allahu liman hamidah. Rabbana wa lakal-hamd",
                translation = "Allah hört den, der Ihn preist. Unser Herr, Dir gebührt alles Lob",
                images = listOf(R.drawable.itidal),
                durationMillis = 6000L
            )
            AppLanguage.TURKISH -> PrayerStep(
                title = "$number. Rekat: Doğrulmak",
                posture = "Ayakta durmak",
                description = "Doğrulun ve söyleyin:",
                arabicText = "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ. رَبَّنَا وَلَكَ الْحَمْدُ",
                transliteration = "Semi’allâhu limen hamideh. Rabbenâ leke’l-hamd",
                translation = "Allah hamd edeni işitti. Rabbimiz, hamd sana mahsustur",
                images = listOf(R.drawable.itidal),
                durationMillis = 6000L
            )
        },
        when (lang) {
            AppLanguage.GERMAN -> PrayerStep(
                title = "Rakat $number: Sujud 1",
                posture = "Niederwerfung",
                description = "Werfen Sie sich nieder und sagen Sie dreimal:",
                arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                transliteration = "Subhana Rabbiyal-A'la",
                translation = "Gepriesen sei mein Herr, der Allerhöchste",
                images = listOf(R.drawable.sujud),
                repeatCount = 3,
                durationMillis = 10000L
            )
            AppLanguage.TURKISH -> PrayerStep(
                title = "$number. Rekat: Secde 1",
                posture = "Secde",
                description = "Secdeye gidin ve üç kez söyleyin:",
                arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                transliteration = "Sübhâne Rabbiye’l-A’lâ",
                translation = "Yüce olan Rabbim her türlü noksan sıfatlardan münezzehtir",
                images = listOf(R.drawable.sujud),
                repeatCount = 3,
                durationMillis = 10000L
            )
        },
        when (lang) {
            AppLanguage.GERMAN -> PrayerStep(
                title = "Rakat $number: Sitzen",
                posture = "Sitzen",
                description = "Setzen Sie sich kurz aufrecht hin und sagen Sie:",
                arabicText = "رَبِّ اغْفِرْ لِي",
                transliteration = "Rabbighfir li",
                translation = "Mein Herr, vergib mir",
                images = listOf(R.drawable.jalsa),
                durationMillis = 4000L
            )
            AppLanguage.TURKISH -> PrayerStep(
                title = "$number. Rekat: Oturuş",
                posture = "Celse",
                description = "Kısa bir süre oturun ve söyleyin:",
                arabicText = "رَبِّ اغْفِرْ لِي",
                transliteration = "Rabbighfir li",
                translation = "Rabbim beni bağışla",
                images = listOf(R.drawable.jalsa),
                durationMillis = 4000L
            )
        },
        when (lang) {
            AppLanguage.GERMAN -> PrayerStep(
                title = "Rakat $number: Sujud 2",
                posture = "Niederwerfung",
                description = "Wiederholen Sie die Niederwerfung dreimal:",
                arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                transliteration = "Subhana Rabbiyal-A'la",
                translation = "Gepriesen sei mein Herr, der Allerhöchste",
                images = listOf(R.drawable.sujud),
                repeatCount = 3,
                durationMillis = 10000L
            )
            AppLanguage.TURKISH -> PrayerStep(
                title = "$number. Rekat: Secde 2",
                posture = "Secde",
                description = "Secdeyi tekrarlayın ve üç kez söyleyin:",
                arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                transliteration = "Sübhâne Rabbiye’l-A’lâ",
                translation = "Yüce olan Rabbim her türlü noksan sıfatlardan münezzehtir",
                images = listOf(R.drawable.sujud),
                repeatCount = 3,
                durationMillis = 10000L
            )
        }
    )

    fun middleTashahhud(lang: AppLanguage) = when (lang) {
        AppLanguage.GERMAN -> PrayerStep(
            title = "Mittlerer Tashahhud",
            posture = "Sitzen",
            description = "Rezitieren Sie den ersten Teil des Tashahhud.",
            arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَواتُ وَالطَّيِّباتُ السَّلامُ عَلَيْكَ أَيُّهَا النَّBِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ السَّلامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّALIHِينَ أَشْهَدُ أَنْ لا إِلَهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            transliteration = "At-tahiyyatu lillahi was-salawatu wat-tayyibatu, as-salamu 'alayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuhu, as-salamu 'alayna wa 'ala 'ibadillahis-salihin. Ashhadu an la ilaha illallah wa ashhadu anna Muhammadan 'abduhu wa rasuluh.",
            translation = "Alle Ehrerweisungen, Gebete und die guten Dinge gebühren Allah. Friede sei mit dir, o Prophet, und die Barmherzigkeit Allahs und Sein Segen. Friede sei mit uns und mit den rechtschaffenen Dienern Allahs. Ich bezeuge, dass es keinen Gott gibt außer Allah, und ich bezeuge, dass Muhammad Sein Diener und Gesandter ist.",
            images = listOf(R.drawable.jalsa),
            durationMillis = 15000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "Tahiyyat (Ara Oturuş)",
            posture = "Oturuş",
            description = "Tahiyyat duasının ilk bölümünü okuyun.",
            arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَواتُ وَالطَّيِّباتُ السَّلامُ عَلَيْكَ أَيُّهَا النَّBِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ السَّلامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّALIHِينَ أَشْهَدُ أَنْ لا إِلَهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            transliteration = "Ettehıyyâtü lillâhi vessalevâtü vettayyıbât. Esselâmü aleyke eyyühen-nebiyyü ve rahmetüllâhi ve berekâtüh. Esselâmü aleynâ ve alâ ibâdillâhis-salihîn. Eşhedü en lâ ilâhe illallâh ve eşhedü enne Muhammeden abdühû ve rasûlüh.",
            translation = "Bütün dualar, senalar, bedeni ve mali ibadetler Allah'a mahsustur. Ey Peygamber! Selam, Allah'ın rahmet ve bereketleri senin üzerine olsun. Selam bizim üzerimize ve Allah'ın bütün salih kulları üzerine olsun. Şahitlik ederim ki Allah'tan başka ilah yoktur. Yine şahitlik ederim ki Muhammed O'nun kulu und elçisidir.",
            images = listOf(R.drawable.jalsa),
            durationMillis = 15000L
        )
    }

    fun finalTashahhud(lang: AppLanguage) = when (lang) {
        AppLanguage.GERMAN -> PrayerStep(
            title = "Abschluss-Tashahhud",
            posture = "Sitzen",
            description = "Rezitieren Sie den vollständigen Tashahhud und Gebete für den Propheten.",
            arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَواتُ وَالطَّيِّباتُ السَّلامُ عَلَيْكَ أَيُّهَا النَّBِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ السَّلامُ عَلَيْنَا وَعَلَى عِبَاد. اللَّهِ الصَّALIHِينَ أَشْهَدُ أَنْ لا إِلَهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ. اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِIDٌ. اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
            transliteration = "At-tahiyyatu lillahi was-salawatu wat-tayyibatu, as-salamu 'alayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuhu, as-salamu 'alayna wa 'ala 'ibadillahis-salihin. Ashhadu an la ilaha illallah wa ashhadu anna Muhammadan 'abduhu wa rasuluh. Allahumma salli 'ala Muhammadin wa 'ala ali Muhammad, kama sallayta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid. Allahumma barik 'ala Muhammadin wa 'ala ali Muhammad, kama barakta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid.",
            translation = "Alle Ehrerweisungen gebühren Allah. Friede sei mit dir, o Prophet... Ich bezeuge, dass es keinen Gott gibt außer Allah und Muhammad Sein Gesandter ist. O Allah, segne Muhammad und die Familie von Muhammad, wie Du Abraham und die Familie von Abraham gesegnet hast. Wahrlich, Du bist der Preiswürdige, der Ruhmreiche. O Allah, gib Muhammad und der Familie von Muhammad Deinen Segen, wie Du Abraham und die Familie von Abraham Deinen Segen gegeben hast. Wahrlich, Du bist der Preiswürdige, der Ruhmreiche.",
            images = listOf(R.drawable.jalsa),
            durationMillis = 25000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "Son Oturuş (Tahiyyat, Salli-Barik)",
            posture = "Oturuş",
            description = "Tahiyyat ve Salli-Barik dualarını okuyun.",
            arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَواتُ وَالطَّيِّباتُ السَّلامُ عَلَيْكَ أَيُّهَا النَّBِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ السَّلامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّALIHِينَ أَشْهَدُ أَنْ لا إِلَهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ. اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ. اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ hamîdün mecîd",
            transliteration = "Ettehıyyâtü... Allâhümme salli alâ Muhammedin ve alâ âli Muhammed. Kemâ salleyte alâ İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd. Allâhümme bârik alâ Muhammedin ve alâ âli Muhammed. Kemâ bârekte alâ İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd.",
            translation = "Tahiyyat... Allah'ım! Hz. Muhammed'e ve onun aline salat et, tıpkı Hz. İbrahim'e ve aline salat ettiğin gibi. Şüphesek Sen, övülmeye layık und şerefi yüce olansın. Allah'ım! Hz. Muhammed'e ve onun aline bereket ver, tıpkı Hz. İbrahim'e ve aline bereket verdiğin gibi. Şüphesiz Sen, övülmeye layık und şerefi yüce olansın.",
            images = listOf(R.drawable.jalsa),
            durationMillis = 25000L
        )
    }

    fun taslim(lang: AppLanguage) = when (lang) {
        AppLanguage.GERMAN -> PrayerStep(
            title = "Taslim (Abschluss)",
            posture = "Kopf drehen",
            description = "Grüßen Sie nach rechts und nach links.",
            arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
            transliteration = "Assalamu alaykum wa rahmatullah",
            translation = "Friede sei mit euch und die Barmherzigkeit Allahs",
            images = listOf(R.drawable.taslim1, R.drawable.taslim2),
            repeatCount = 2,
            durationMillis = 8000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "Selam",
            posture = "Başını çevirmek",
            description = "Sağa ve sola selam verin.",
            arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
            transliteration = "Esselâmu aleyküm ve rahmetullâh",
            translation = "Allah'ın selamı ve rahmeti üzerinize olsun",
            images = listOf(R.drawable.taslim1, R.drawable.taslim2),
            repeatCount = 2,
            durationMillis = 8000L
        )
    }
}

fun getAllPrayers(lang: AppLanguage) = listOf(
    PrayerConfig(
        id = "fajr", name = if (lang == AppLanguage.GERMAN) "Fajr" else "Sabah",
        description = if (lang == AppLanguage.GERMAN) "Morgengebet (2 Rakat)" else "Sabah namazı (2 Rekat)",
        rakat = 2,
        steps = listOf(CommonSteps.niyyah(if (lang == AppLanguage.GERMAN) "Fajr" else "Sabah", 2, lang), CommonSteps.takbir(lang)) +
                CommonSteps.rakatSteps(1, lang) + CommonSteps.rakatSteps(2, lang) +
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.taslim(lang))
    ),
    PrayerConfig(
        id = "dhuhr", name = if (lang == AppLanguage.GERMAN) "Dhuhr" else "Öğle",
        description = if (lang == AppLanguage.GERMAN) "Mittagsgebet (4 Rakat)" else "Öğle namazı (4 Rekat)",
        rakat = 4,
        steps = listOf(CommonSteps.niyyah(if (lang == AppLanguage.GERMAN) "Dhuhr" else "Öğle", 4, lang), CommonSteps.takbir(lang)) +
                CommonSteps.rakatSteps(1, lang) + CommonSteps.rakatSteps(2, lang) + listOf(CommonSteps.middleTashahhud(lang)) +
                CommonSteps.rakatSteps(3, lang, false) + CommonSteps.rakatSteps(4, lang, false) +
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.taslim(lang))
    ),
    PrayerConfig(
        id = "asr", name = if (lang == AppLanguage.GERMAN) "Asr" else "İkindi",
        description = if (lang == AppLanguage.GERMAN) "Nachmittagsgebet (4 Rakat)" else "İkindi namazı (4 Rekat)",
        rakat = 4,
        steps = listOf(CommonSteps.niyyah(if (lang == AppLanguage.GERMAN) "Asr" else "İkindi", 4, lang), CommonSteps.takbir(lang)) +
                CommonSteps.rakatSteps(1, lang) + CommonSteps.rakatSteps(2, lang) + listOf(CommonSteps.middleTashahhud(lang)) +
                CommonSteps.rakatSteps(3, lang, false) + CommonSteps.rakatSteps(4, lang, false) +
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.taslim(lang))
    ),
    PrayerConfig(
        id = "maghrib", name = if (lang == AppLanguage.GERMAN) "Maghrib" else "Akşam",
        description = if (lang == AppLanguage.GERMAN) "Abendgebet (3 Rakat)" else "Akşam namazı (3 Rekat)",
        rakat = 3,
        steps = listOf(CommonSteps.niyyah(if (lang == AppLanguage.GERMAN) "Maghrib" else "Akşam", 3, lang), CommonSteps.takbir(lang)) +
                CommonSteps.rakatSteps(1, lang) + CommonSteps.rakatSteps(2, lang) + listOf(CommonSteps.middleTashahhud(lang)) +
                CommonSteps.rakatSteps(3, lang, false) +
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.taslim(lang))
    ),
    PrayerConfig(
        id = "isha", name = if (lang == AppLanguage.GERMAN) "Isha" else "Yatsı",
        description = if (lang == AppLanguage.GERMAN) "Nachtgebet (4 Rakat)" else "Yatsı namazı (4 Rekat)",
        rakat = 4,
        steps = listOf(CommonSteps.niyyah(if (lang == AppLanguage.GERMAN) "Isha" else "Yatsı", 4, lang), CommonSteps.takbir(lang)) +
                CommonSteps.rakatSteps(1, lang) + CommonSteps.rakatSteps(2, lang) + listOf(CommonSteps.middleTashahhud(lang)) +
                CommonSteps.rakatSteps(3, lang, false) + CommonSteps.rakatSteps(4, lang, false) +
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.taslim(lang))
    )
)

fun getRakatVariants(lang: AppLanguage) = listOf(
    PrayerConfig(
        id = "2rakat", name = if (lang == AppLanguage.GERMAN) "2 Rakat Gebet" else "2 Rekat Namaz",
        description = if (lang == AppLanguage.GERMAN) "z.B. Fajr oder Sunna" else "Örn. Sabah veya Sünnet",
        rakat = 2, steps = emptyList()
    ),
    PrayerConfig(
        id = "3rakat", name = if (lang == AppLanguage.GERMAN) "3 Rakat Gebet" else "3 Rekat Namaz",
        description = if (lang == AppLanguage.GERMAN) "z.B. Maghrib" else "Örn. Akşam",
        rakat = 3, steps = emptyList()
    ),
    PrayerConfig(
        id = "4rakat", name = if (lang == AppLanguage.GERMAN) "4 Rakat Gebet" else "4 Rekat Namaz",
        description = if (lang == AppLanguage.GERMAN) "z.B. Dhuhr, Asr, Isha" else "Örn. Öğle, İkindi, Yatsı",
        rakat = 4, steps = emptyList()
    )
)

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

    SharedTransitionLayout(modifier = modifier) {
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
                PrayerSelectionScreen(
                    title = if (lang == AppLanguage.GERMAN) "Automatisches Mitbeten" else "Otomatik Takip",
                    prayers = allPrayers,
                    onPrayerSelected = { selectedPrayer = it },
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
                    GuidedPrayerSession(
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

    SharedTransitionLayout(modifier = modifier) {
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
                PrayerSelectionScreen(
                    title = if (lang == AppLanguage.GERMAN) "Schritt für Schritt" else "Adım Adım",
                    prayers = variants,
                    onPrayerSelected = { selectedPrayer = it },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent
                )
            } else {
                val condensedSteps = remember(targetState, lang) {
                    val rCount = targetState.rakat
                    val qiyamStep = CommonSteps.rakatSteps(1, lang)[0]
                    listOf(
                        CommonSteps.takbir(lang).copy(title = if (lang == AppLanguage.GERMAN) "Beginn (Takbir)" else "Başlangıç (Tekbir)"),
                        PrayerStep(
                            title = if (lang == AppLanguage.GERMAN) "Die ersten zwei Rakat" else "İlk İki Rekat",
                            posture = if (lang == AppLanguage.GERMAN) "Qiyam (Stehen)" else "Kıyam (Ayakta durmak)",
                            description = if (lang == AppLanguage.GERMAN) "Hier rezitieren Sie die al-Fatiha und eine weitere Sure Ihrer Wahl." else "Burada Fatiha suresini ve seçtiğiniz bir sureyi okuyun.",
                            arabicText = qiyamStep.arabicText,
                            transliteration = qiyamStep.transliteration,
                            translation = qiyamStep.translation
                        ),
                        CommonSteps.rakatSteps(1, lang)[1].copy(title = if (lang == AppLanguage.GERMAN) "Die Verbeugung" else "Rüku", posture = "Ruku", description = if (lang == AppLanguage.GERMAN) "Beugen Sie sich vor und sagen Sie dreimal die Lobpreisung." else "Eğilin ve üç kez tesbihatı söyleyin."),
                        CommonSteps.rakatSteps(1, lang)[2].copy(title = if (lang == AppLanguage.GERMAN) "Das Aufrichten" else "Doğrulmak", posture = "I'dtidal"),
                        CommonSteps.rakatSteps(1, lang)[3].copy(title = if (lang == AppLanguage.GERMAN) "Die Niederwerfung" else "Secde", posture = "Sujud", description = if (lang == AppLanguage.GERMAN) "Sagen Sie dreimal die Lobpreisung. Dies geschieht zweimal pro Rakat." else "Üç kez tesbihatı söyleyin. Bu her rekatta iki kez yapılır."),
                        CommonSteps.rakatSteps(1, lang)[4].copy(title = if (lang == AppLanguage.GERMAN) "Das Sitzen" else "Oturuş", posture = "Jalsa", description = if (lang == AppLanguage.GERMAN) "Das kurze Sitzen zwischen den beiden Niederwerfungen." else "İki secde arasındaki kısa oturuş."),
                        if (rCount > 2) CommonSteps.middleTashahhud(lang).copy(title = if (lang == AppLanguage.GERMAN) "Mittlerer Tashahhud" else "Ara Oturuş (Tahiyyat)", description = if (lang == AppLanguage.GERMAN) "Wird nach der 2. Rakat rezitiert (bei 3 oder 4 Rakat Gebeten)." else "2. rekattan sonra okunur (3 veya 4 rekatlı namazlarda).") else null,
                        if (rCount > 2) PrayerStep(
                            title = if (lang == AppLanguage.GERMAN) "Die hinteren Rakat (3. & 4.)" else "Sonraki Rekatlar (3. ve 4.)",
                            posture = if (lang == AppLanguage.GERMAN) "Qiyam (Stehen)" else "Kıyam (Ayakta durmak)",
                            description = if (lang == AppLanguage.GERMAN) "In der 3. und 4. Rakat wird nur die al-Fatiha leise rezitiert." else "3. ve 4. rekatta sadece Fatiha suresi sessizce okunur.",
                            arabicText = qiyamStep.arabicText,
                            transliteration = qiyamStep.transliteration,
                            translation = qiyamStep.translation
                        ) else null,
                        CommonSteps.finalTashahhud(lang).copy(title = if (lang == AppLanguage.GERMAN) "Abschluss-Tashahhud" else "Son Oturuş (Tahiyyat)", description = if (lang == AppLanguage.GERMAN) "Der vollständige Tashahhud am Ende des Gebets." else "Namazın sonundaki tam Tahiyyat."),
                        CommonSteps.taslim(lang).copy(title = if (lang == AppLanguage.GERMAN) "Der Friedensgruß (Taslim)" else "Selam")
                    ).filterNotNull()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            rememberSharedContentState(key = targetState.id),
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
                if (lang == AppLanguage.GERMAN) "Diese Liste zeigt die verschiedenen Bestandteile des Gebets ohne Wiederholungen der Rakat-Blöcke." else "Bu liste, rekat tekrarları olmaksızın namazın farklı bölümlerini göstermektedir.",
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
    onPrayerSelected: (PrayerConfig) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
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
                                rememberSharedContentState(key = prayer.id),
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
            CountdownUI(countdownValue)
        } else if (currentStepIndex < steps.size) {
            ActiveStepUI(steps[currentStepIndex], currentStepIndex, steps.size, onExit)
        }
    }
}

@Composable
fun CountdownUI(value: Int) {
    val lang = LocalAppLanguage.current
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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
fun ActiveStepUI(step: PrayerStep, index: Int, total: Int, onExit: () -> Unit) {
    val lang = LocalAppLanguage.current
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
        // Removed the outer Spacer that was shrinking the card
    }
}

@Preview(showBackground = true)
@Composable
fun PrayerGuidePreview() {
    MyApplicationTheme {
        PrayerLearnScreen(tts = null, isTtsReady = false)
    }
}
