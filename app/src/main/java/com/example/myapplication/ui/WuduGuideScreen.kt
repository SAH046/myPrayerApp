package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

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

/**
 * A screen that provides a step-by-step guide on how to perform Wudu.
 *
 * The content is automatically localized based on the [LocalAppLanguage].
 *
 * @param onBack Callback to be invoked when the back button is pressed.
 * @param modifier The modifier to be applied to the layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WuduGuideScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val steps = remember(lang) {
        if (lang == AppLanguage.GERMAN) {
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

    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text(if (lang == AppLanguage.GERMAN) "Wudu Anleitung" else "Abdest Rehberi", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(steps) { index, step ->
                WuduStepItem(index + 1, step)
            }
        }
    }
}

/**
 * A list item representing a single step of Wudu.
 *
 * @param number The step number to be displayed in a circle.
 * @param step The [WuduStep] data to display.
 */
@Composable
fun WuduStepItem(number: Int, step: WuduStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WuduGuideScreenPreview() {
    MyApplicationTheme {
        WuduGuideScreen(onBack = {})
    }
}
