package com.example.myapplication.data

import com.example.myapplication.ui.AppLanguage

/**
 * Data class representing a single step in the Wudu (ablution) process.
 *
 * @property title The name of the step.
 * @property description A detailed explanation of how to perform the step.
 */
data class WuduStep(
    val title: String,
    val description: String
)

object WuduDataProvider {
    fun getWuduSteps(lang: AppLanguage): List<WuduStep> = if (lang == AppLanguage.GERMAN) {
        listOf(
            WuduStep("Absicht (Niyyah)", "Fassen Sie die Absicht, Wudu für das Gebet zu vollziehen und sagen Sie 'Bismillah'."),
            WuduStep("Hände waschen", "Waschen Sie beide Hände dreimal bis zu den Handgelenken, auch zwischen den Fingern."),
            WuduStep("Mund ausspülen", "Spülen Sie den Mund dreimal gründlich mit Wasser aus."),
            WuduStep("Nase reinigen", "Ziehen Sie dreimal Wasser in die Nase und schnäuzen Sie es mit der linken Hand wieder aus."),
            WuduStep("Gesicht waschen", "Waschen Sie das gesamte Gesicht dreimal (vom Haaransatz bis zum Kinn und von Ohr zu Ohr)."),
            WuduStep("Arme waschen", "Waschen Sie den rechten Arm dreimal bis einschließlich zum Ellbogen, dann den linken Arm ebenso."),
            WuduStep("Kopf streichen (Masah)", "Streichen Sie einmal mit feuchten Händen über den Kopf (von vorne nach hinten und zurück)."),
            WuduStep("Ohren reinigen", "Reinigen Sie mit den Zeigefingern das Innere und mit den Daumen das Äußere der Ohren."),
            WuduStep("Füße waschen", "Waschen Sie den rechten Fuß dreimal bis einschließlich zum Knöchel, dann den linken Fuß ebenso. Achten Sie auf die Zehenzwischenräume.")
        )
    } else {
        listOf(
            WuduStep("Niyet", "Namaz için abdest almaya niyet edin ve 'Besmele' çekin."),
            WuduStep("Elleri Yıkamak", "Ellerinizi bileklere kadar, parmak aralarını da dahil ederek üç kez yıkayın."),
            WuduStep("Ağza Su Vermek", "Ağzınızı suyla üç kez iyice çalkalayın."),
            WuduStep("Buruna Su Vermek", "Burnunuza üç kez su çekin ve sol elinizle sümkürün."),
            WuduStep("Yüzü Yıkamak", "Tüm yüzünüzü üç kez yıkayın (saç diplerinden çeneye, kulak memesinden diğerine kadar)."),
            WuduStep("Kolları Yıkamak", "Önce sağ kolunuzu dirsekle beraber üç kez, sonra sol kolunuzu aynı şekilde yıkayın."),
            WuduStep("Başı Meshetmek", "Islak ellerinizle başınızın üstünü bir kez meshedin (önden arkaya ve geri)."),
            WuduStep("Kulakları Temizlemek", "İşaret parmaklarınızla kulağın içini, baş parmaklarınızla dışını temizleyin."),
            WuduStep("Ayakları Yıkamak", "Sağ ayağınızı topuklarla beraber üç kez yıkayın, sonra sol ayağınızı da aynı şekilde. Parmak aralarını yıkamaya dikkat edin.")
        )
    }
}
