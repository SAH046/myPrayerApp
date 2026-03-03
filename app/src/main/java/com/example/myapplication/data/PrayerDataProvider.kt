package com.example.myapplication.data

import com.example.myapplication.R
import com.example.myapplication.ui.AppLanguage
import java.util.Locale

object CommonSteps {
    fun niyyah(prayerName: String, rakat: Int, lang: AppLanguage) = when (lang) {
        AppLanguage.GERMAN -> PrayerStep(
            title = "Niyyah (Absicht)",
            posture = "Qiyam",
            description = "Fassen Sie die Absicht für das $prayerName Gebet ($rakat Rakat) fest in Ihrem Herzen.",
            images = listOf(R.drawable.niyyah),
            durationMillis = 6000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "Niyet",
            posture = "Kıyam",
            description = "$prayerName namazı için ($rakat rekat) niyet edin.",
            images = listOf(R.drawable.niyyah),
            durationMillis = 6000L
        )
    }

    fun takbir(lang: AppLanguage) = when (lang) {
        AppLanguage.GERMAN -> PrayerStep(
            title = "Takbirat al-Ihram",
            posture = "Takbiratu-l-ihram",
            description = "Eröffnendes Takbir: Aufrecht stehen in Richtung Mekka, beide Hände in Höhe des Halses oder der Ohren hebend, Handflächen nach vorn geöffnet. Sagen Sie einmalig:",
            arabicText = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translation = "Allah ist am größten",
            images = listOf(R.drawable.takbirat),
            durationMillis = 4000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "İftitah Tekbiri",
            posture = "İftitah Tekbiri",
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
                posture = "Qiyam",
                description = "Aufrechtes Stehen: Hände zwischen Brust und Nabel verschränkt, rechte Hand auf linken Unterarm legen. Rezitieren Sie die al-Fatiha" + if (withExtraSura) " und eine weitere Sure." else ".",
                arabicText = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ. الْhَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ. الرَّhْمَنِ الرَّhِيمِ. مَالِكِ يَوْمِ الدِّينِ. إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ. اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ. صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْhِمْ غَيْرِ الْمَغْضُوبِ عَلَيْhِمْ وَلَا الضَّالِّينَ",
                transliteration = "Bismillahir-rahmanir-rahim. Al-hamdu lillahi rabbil-'alamin. Ar-rahmanir-rahim. Maliki yawmid-din. Iyyaka na'budu wa iyyaka nasta'in. Ihdinas-siratal-mustaqim. Siratal-ladhina an'amta 'alayhim, ghayril-maghdubi 'alayhim walad-dallin.",
                translation = "Im Namen Allahs, des Gnädigen, des Barmherzigen. Alles Lob gebührt Allah, dem Herrn der Welten. Dem Gnädigen, dem Barmherzigen. Herrscher am Tage des Gerichts. Dir allein dienen wir, und zu Dir allein flehen wir um Hilfe. Führe uns den geraden Weg, den Weg derer, denen Du Gnade erwiesen hast, nicht den Weg derer, die Deinen Zorn erregt haben, und nicht den Weg der Irrenden.",
                images = listOf(R.drawable.qiyam),
                durationMillis = 20000L
            )
            AppLanguage.TURKISH -> PrayerStep(
                title = "$number. Rekat: Kıyam",
                posture = "Kıyam",
                description = "Fatiha suresini" + if (withExtraSura) " ve ek bir sure okuyun." else " okuyun.",
                arabicText = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ. الْhَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ. الرَّhْمَن. الرَّhِيمِ. مَالِكِ يَوْمِ الدِّينِ. إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ. اهْدِنَا الصِّIREKAL MÜSTAKİM. SIRÂTALLEZÎNE EN'AMTE ALEYHİM GAYRİLMAĞDÛBİ ALEYHİM VELEDDÂLLÎN.",
                transliteration = "Bismillâhirrahmânirrahîm. Elhamdü lillâhi rabbil'alemin. Errahmânirrahîm. Mâliki yevmiddîn. İyyâke na'büdü ve iyyâke neste'în. İhdinessırâtel müstakîm. Sırâtallezîne en'amte aleyhim gayrilmağdûbi aleyhim veleddâllîn.",
                translation = "Rahman ve Rahim olan Allah'ın adıyla. Hamd, Alemlerin Rabbi Allah'a mahsustur. O, Rahman ve Rahimdir. Hesap ve ceza gününün malikidir. Yalnız Sana ibadet eder ve yalnız Senden yardım dileriz. Bizi doğru yola ilet; kendilerine nimet verdiklerinin yoluna, gazaba uğrayanlarınkine ve sapkınlarınkine değil.",
                images = listOf(R.drawable.qiyam),
                durationMillis = 20000L
            )
        },
        when (lang) {
            AppLanguage.GERMAN -> PrayerStep(
                title = "Rakat $number: Ruku",
                posture = "Ruku‘",
                description = "Verbeugen: Oberkörper nach vorn beugen, Hände auf den Kniescheiben ablegen. Sagen Sie dreimal:",
                arabicText = "سُبْhَانَ رَبِّيَ الْعَظِيمِ",
                transliteration = "Subhana Rabbiyal-Adhim",
                translation = "Gepriesen sei mein Herr, der Gewaltige",
                images = listOf(R.drawable.ruku),
                repeatCount = 3,
                durationMillis = 8000L
            )
            AppLanguage.TURKISH -> PrayerStep(
                title = "$number. Rekat: Rüku",
                posture = "Rüku",
                description = "Eğilin ve üç kez söyleyin:",
                arabicText = "سُبْhَانَ رَبِّيَ الْعَظِيمِ",
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
                posture = "Qama",
                description = "Aufrechtes Stehen: Nach der Verbeugung wieder gerade stehen, Hände an den Seiten. Sagen Sie:",
                arabicText = "سَمِعَ اللَّهُ لِمَنْ hَمِدَهُ. رَبَّنَا وَلَكَ الْhَمْدُ",
                transliteration = "Sami'Allahu liman hamidah. Rabbana wa lakal-hamd",
                translation = "Allah hört den, der Ihn preist. Unser Herr, Dir gebührt alles Lob",
                images = listOf(R.drawable.itidal),
                durationMillis = 6000L
            )
            AppLanguage.TURKISH -> PrayerStep(
                title = "$number. Rekat: Doğrulmak",
                posture = "Kıyam",
                description = "Doğrulun ve söyleyin:",
                arabicText = "سَمِعَ اللَّهُ لِمَنْ hَمِدَهُ. رَبَّنَا وَلَكَ الْhَمْدُ",
                transliteration = "Semi’allâhu limen hamideh. Rabbenâ leke’l-hamd",
                translation = "Allah hamd edeni işitti. Rabbimiz, hamd sana mahsustur",
                images = listOf(R.drawable.itidal),
                durationMillis = 6000L
            )
        },
        when (lang) {
            AppLanguage.GERMAN -> PrayerStep(
                title = "Rakat $number: Sujud 1",
                posture = "Sadschda",
                description = "Niederwerfung: Stirn, Nase, Handflächen, Knie und Zehenspitzen berühren den Boden. Sagen Sie dreimal:",
                arabicText = "سُبْhَانَ رَبِّيَ الْأَعْلَى",
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
                arabicText = "سُبْhَانَ رَبِّيَ الْأَعْلَى",
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
                posture = "Dschalsa",
                description = "Aufsitzen: Auf Knien und Füßen sitzen, Hände auf den Knien. Sagen Sie:",
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
                posture = "Sadschda",
                description = "Niederwerfung: Stirn, Nase, Handflächen, Knie und Zehenspitzen berühren den Boden. Sagen Sie dreimal:",
                arabicText = "سُبْhَانَ رَبِّيَ الْأَعْلَى",
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
                arabicText = "سُبْhَانَ رَبِّيَ الْأَعْلَى",
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
            posture = "Dschalsa",
            description = "Aufsitzen: Auf Knien und Füßen sitzen, Hände auf den Knien. Rezitieren Sie den ersten Teil des Tashahhud.",
            arabicText = "التَّhِيَّاتُ لِلَّهِ وَالصَّلَواتُ وَالطَّيِّباتُ السَّلامُ عَلَيْكَ أَيُّهَا النَّBِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ السَّلامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّALIHِينَ أَشْhَدُ أَنْ لا إِلَهَ إِلَّا اللَّهُ وَأَشْhَدُ أَنْ لا إِلَهَ إِلَّا اللَّهُ وَأَشْhَدُ أَنَّ مُhَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            transliteration = "At-tahiyyatu lillahi was-salawatu wat-tayyibatu, as-salamu 'alayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuhu, as-salamu 'alayna wa 'ala 'ibadillahis-salihin. Ashhadu an la ilaha illallah wa ashhadu anna Muhammadan 'abduhu wa rasuluh.",
            translation = "Alle Ehrerweisungen, Gebete und die guten Dinge gebühren Allah. Friede sei mit dir, o Prophet, und die Barmherzigkeit Allahs und Sein Segen. Friede sei mit uns und mit den rechtschaffenen Dienern Allahs. Ich bezeuge, dass es keinen Gott gibt außer Allah, und ich bezeuge, dass Muhammad Sein Diener and Gesandter ist.",
            images = listOf(R.drawable.jalsa),
            durationMillis = 15000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "Tahiyyat (Ara Oturuş)",
            posture = "Oturuş",
            description = "Tahiyyat duasının ilk bölümünü okuyun.",
            arabicText = "التَّhِيَّاتُ lillâhi vessalevâtü vettayyıbât. Esselâmü aleyke eyyühen-nebiyyü ve rahmetüllâhi ve berekâtüh. Esselâmü aleynâ ve alâ ibâdillâhis-salihîn. Eşhedü en lâ ilâhe illallâh ve eşhedü enne Muhammeden abdühû ve rasûlüh.",
            transliteration = "Ettehıyyâtü lillâhi vessalevâtü vettayyıbât. Esselâmü aleyke eyyühen-nebiyyü ve rahmetüllâhi ve berekâtüh. Esselâmü aleynâ ve alâ ibâdillâhis-salihîn. Eşhedü en lâ ilâhe illallâh ve eşhedü enne Muhammeden abdühû ve rasûlüh.",
            translation = "Bütün dualar, senalar, bedeni ve mali ibadetler Allah'a mahsustur. Ey Peygamber! Selam, Allah'ın rahmet ve bereketleri senin üzerine olsun. Selam bizim üzerimize ve Allah'ın bütün salih kulları üzerine olsun. Şahitlik ederim ki Allah'tan başka ilah yoktur. Yine shahitlik ederim ki Muhammed O'nun kulu und elçisidir.",
            images = listOf(R.drawable.jalsa),
            durationMillis = 15000L
        )
    }

    fun finalTashahhud(lang: AppLanguage) = when (lang) {
        AppLanguage.GERMAN -> PrayerStep(
            title = "Abschluss-Tashahhud",
            posture = "Dschalsa",
            description = "Aufsitzen: Auf Knien und Füßen sitzen, Hände auf den Knien. Rezitieren Sie den vollständigen Tashahhud und Gebete für den Propheten.",
            arabicText = "التَّhِيَّاتُ لِلَّهِ وَالصَّلَواتُ وَالطَّيِّباتُ السَّلامُ عَلَيْكَ أَيُّهَا النَّBِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ السَّلامُ عَلَيْنَا وَعَلَى عِبَاد. اللَّهِ الصَّALIHِينَ أَشْhَدُ أَنْ لا إِلَهَ إِلَّا اللَّهُ وَأَشْhَدُ أَنَّ مُhَمَّدًا عَبْدُهُ وَرَسُولُهُ. اللَّhُمَّ صَلِّ عَلَى مُhَمَّدٍ وَعَلَى آلِ مُhَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ hَمِيدٌ مَجِIDٌ. اللَّhُمَّ بَارِكْ عَلَى مُhَمَّدٍ وَعَلَى آلِ مُhَمَّدٍ كَمَا  بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ hَمِيدٌ مَجِيدٌ",
            transliteration = "At-tahiyyatu lillahi was-salawatu wat-tayyibatu, as-salamu 'alayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuhu, as-salamu 'alayna wa 'ala 'ibadillahis-salihin. Ashhadu an la ilaha illallah wa ashhadu anna Muhammadan 'abduhu wa rasuluh. Allahumma salli 'ala Muhammadin wa 'ala ali Muhammad, kama sallayta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid. Allahumma barik 'ala Muhammadin wa 'ala ali Muhammad, kama barakta 'ala Ibrahima wa 'ala ali Ibrahim, innaka Hamidun Majid.",
            translation = "Alle Ehrerweisungen gebühren Allah. Friede sei mit dir, o Prophet... Ich bezeuge, dass es keinen Gott gibt außer Allah und Muhammad Sein Gesandter ist. O Allah, segne Muhammad und die Familie von Muhammad, wie Du Abraham und die Familie von Abraham gesegnet hast. Wahrlich, Du bist der Preiswürdige, der Ruhmreiche. O Allah, gib Muhammad und der Familie von Muhammad Deinen Segen, wie Du Abraham und die Familie von Abraham Deinen Segen gegeben hast. Wahrlich, Du bist der Preiswürdige, der Ruhmreiche.",
            images = listOf(R.drawable.jalsa),
            durationMillis = 25000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "Son Oturuş (Tahiyyat, Salli-Barik)",
            posture = "Oturuş",
            description = "Tahiyyat ve Salli-Barik dualarını okuyun.",
            arabicText = "التَّhِيَّاتُ لِلَّهِ وَالصَّلَواتُ وَالطَّيِّباتُ السَّلامُ عَلَيْكَ أَيُّهَا النَّBِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ السَّلامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّALIHِينَ أَشْhَدُ أَنْ لا إِلَهَ إِلَّا اللَّهُ وَأَشْhَدُ أَنْ لا إِلَهَ إِلَّا اللَّهُ وَأَشْhَدُ أَنَّ مُhَمَّدًا عَبْدُهُ وَرَسُولُهُ. اللَّhُمَّ صَلِّ عَلَى مُhَمَّدٍ وَعَلَى آلِ مُhَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ hَمِيدٌ مَجِيدٌ. اللَّhُمَّ بَارِكْ عَلَى مُhَمَّدٍ وَعَلَى آلِ مُhَمَّدٍ كَمَا bârekte alâ İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd.",
            transliteration = "Ettehıyyâtü... Allâhümme salli alâ Muhammedin ve alâ âli Muhammed. Kemâ salleyte alâ İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd. Allâhümme bârik alâ Muhammedin ve alâ âli Muhammed. Kemâ bârekte alâ İbrâhîme ve alâ âli İbrâhîm. İnneke hamîdün mecîd.",
            translation = "Tahiyyat... Allah'ım! Hz. Muhammed'e ve onun aline salat et, tıpkı Hz. İbrahim'e ve aline salat ettiğin gibi. Şüphesek Sen, övülmeye layık und sherefi yüce olansın. Allah'ım! Hz. Muhammed'e ve onun aline bereket ver, tıpkı Hz. İbrahim'e ve aline bereket verdiğin gibi. Shüphesiz Sen, övülmeye layık und sherefi yüce olansın.",
            images = listOf(R.drawable.jalsa),
            durationMillis = 25000L
        )
    }

    fun finalTaslim(lang: AppLanguage) = when (lang) {
        AppLanguage.GERMAN -> PrayerStep(
            title = "Taslim (Abschluss)",
            posture = "Salam",
            description = "Friedensgruß: Wie Dschalsa, dabei Kopf erst nach rechts, dann nach links wenden.",
            arabicText = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
            transliteration = "Assalamu alaykum wa rahmatullah",
            translation = "Friede sei mit euch und die Barmherzigkeit Allahs",
            images = listOf(R.drawable.taslim1, R.drawable.taslim2),
            repeatCount = 2,
            durationMillis = 8000L
        )
        AppLanguage.TURKISH -> PrayerStep(
            title = "Selam",
            posture = "Selam",
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
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.finalTaslim(lang))
    ),
    PrayerConfig(
        id = "dhuhr", name = if (lang == AppLanguage.GERMAN) "Dhuhr" else "Öğle",
        description = if (lang == AppLanguage.GERMAN) "Mittagsgebet (4 Rakat)" else "Öğle namazı (4 Rekat)",
        rakat = 4,
        steps = listOf(CommonSteps.niyyah(if (lang == AppLanguage.GERMAN) "Dhuhr" else "Öğle", 4, lang), CommonSteps.takbir(lang)) +
                CommonSteps.rakatSteps(1, lang) + CommonSteps.rakatSteps(2, lang) + listOf(CommonSteps.middleTashahhud(lang)) +
                CommonSteps.rakatSteps(3, lang, false) + CommonSteps.rakatSteps(4, lang, false) +
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.finalTaslim(lang))
    ),
    PrayerConfig(
        id = "asr", name = if (lang == AppLanguage.GERMAN) "Asr" else "İkindi",
        description = if (lang == AppLanguage.GERMAN) "Nachmittagsgebet (4 Rakat)" else "İkindi namazı (4 Rekat)",
        rakat = 4,
        steps = listOf(CommonSteps.niyyah(if (lang == AppLanguage.GERMAN) "Asr" else "İkindi", 4, lang), CommonSteps.takbir(lang)) +
                CommonSteps.rakatSteps(1, lang) + CommonSteps.rakatSteps(2, lang) + listOf(CommonSteps.middleTashahhud(lang)) +
                CommonSteps.rakatSteps(3, lang, false) + CommonSteps.rakatSteps(4, lang, false) +
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.finalTaslim(lang))
    ),
    PrayerConfig(
        id = "maghrib", name = if (lang == AppLanguage.GERMAN) "Maghrib" else "Akşam",
        description = if (lang == AppLanguage.GERMAN) "Abendgebet (3 Rakat)" else "Akşam namazı (3 Rekat)",
        rakat = 3,
        steps = listOf(CommonSteps.niyyah(if (lang == AppLanguage.GERMAN) "Maghrib" else "Akşam", 3, lang), CommonSteps.takbir(lang)) +
                CommonSteps.rakatSteps(1, lang) + CommonSteps.rakatSteps(2, lang) + listOf(CommonSteps.middleTashahhud(lang)) +
                CommonSteps.rakatSteps(3, lang, false) +
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.finalTaslim(lang))
    ),
    PrayerConfig(
        id = "isha", name = if (lang == AppLanguage.GERMAN) "Isha" else "Yatsı",
        description = if (lang == AppLanguage.GERMAN) "Nachtgebet (4 Rakat)" else "Yatsı namazı (4 Rekat)",
        rakat = 4,
        steps = listOf(CommonSteps.niyyah(if (lang == AppLanguage.GERMAN) "Isha" else "Yatsı", 4, lang), CommonSteps.takbir(lang)) +
                CommonSteps.rakatSteps(1, lang) + CommonSteps.rakatSteps(2, lang) + listOf(CommonSteps.middleTashahhud(lang)) +
                CommonSteps.rakatSteps(3, lang, false) + CommonSteps.rakatSteps(4, lang, false) +
                listOf(CommonSteps.finalTashahhud(lang), CommonSteps.finalTaslim(lang))
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
