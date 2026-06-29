package com.example.splixter.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.splixter.data.AppStep
import com.example.splixter.data.PersonBreakdown
import com.example.splixter.ui.SplitterUiState
import com.example.splixter.ui.SplitterViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/** Programmatically draws the receipt into a Bitmap using Android Canvas. */
fun buildReceiptBitmap(
    uiState: SplitterUiState,
    breakdowns: List<PersonBreakdown>,
    grandTotalSum: Double
): Bitmap {
    val width = 900
    val margin = 48f
    val lineHeight = 44f
    val smallLineHeight = 34f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Measure total height
    var totalLines = 0
    totalLines += 6 // header block
    for (b in breakdowns) {
        totalLines += 2 // name + spacer
        totalLines += b.items.size
        val hasDis = b.discountShare > 0
        if (hasDis) totalLines += 1
        totalLines += 1 // tax+tip
        totalLines += 1 // divider
    }
    totalLines += 3 // grand total

    val height = (margin * 2 + totalLines * lineHeight + 60).toInt().coerceAtLeast(600)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.parseColor("#FFFDF7")) // parchment

    var y = margin + lineHeight

    fun drawText(text: String, x: Float, bold: Boolean = false, size: Float = 28f, color: Int = android.graphics.Color.parseColor("#222222"), align: Paint.Align = Paint.Align.LEFT) {
        paint.textSize = size
        paint.typeface = if (bold) Typeface.MONOSPACE.also { paint.isFakeBoldText = true } else Typeface.MONOSPACE.also { paint.isFakeBoldText = false }
        paint.color = color
        paint.textAlign = align
        canvas.drawText(text, x, y, paint)
    }

    fun drawDivider() {
        paint.color = android.graphics.Color.parseColor("#CCCCCC")
        paint.strokeWidth = 2f
        canvas.drawLine(margin, y, (width - margin), y, paint)
        y += lineHeight * 0.6f
    }

    // Title
    drawText("SPLIXTER RECEIPT", width / 2f, bold = true, size = 40f, color = android.graphics.Color.parseColor("#1A1A1A"), align = Paint.Align.CENTER)
    y += lineHeight
    val paidPerson = uiState.people.find { it.id == uiState.paidByPersonId }
    val paidName = paidPerson?.let { "${it.name}" } ?: "N/A"
    drawText("Paid by: $paidName", width / 2f, size = 26f, color = android.graphics.Color.parseColor("#555555"), align = Paint.Align.CENTER)
    y += lineHeight * 0.8f
    drawDivider()
    y += lineHeight * 0.4f

    for (b in breakdowns) {
        // Person name row
        drawText("${b.person.emoji} ${b.person.name.uppercase()}", margin, bold = true, size = 32f)
        paint.color = android.graphics.Color.parseColor("#111111")
        paint.textSize = 32f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("₹${String.format(Locale.US, "%.2f", b.grandTotal)}", width - margin, y, paint)
        y += lineHeight

        // Items
        for ((item, costShare) in b.items) {
            val cat = if (item.category == com.example.splixter.data.ItemCategory.LIQUOR) "🍺 " else "🍕 "
            drawText("  $cat${item.name}", margin + 20, size = 24f, color = android.graphics.Color.parseColor("#666666"))
            paint.color = android.graphics.Color.parseColor("#666666")
            paint.textSize = 24f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("₹${String.format(Locale.US, "%.2f", costShare)}", width - margin, y, paint)
            y += smallLineHeight
        }

        // Discounts
        val totalDiscShare = b.discountShare
        if (totalDiscShare > 0.001) {
            drawText("  - Discount", margin + 20, size = 24f, color = android.graphics.Color.parseColor("#22A85A"))
            paint.color = android.graphics.Color.parseColor("#22A85A")
            paint.textSize = 24f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("-₹${String.format(Locale.US, "%.2f", totalDiscShare)}", width - margin, y, paint)
            y += smallLineHeight
        }

        // Tax + Tip
        drawText("  + Tax & Tip", margin + 20, size = 24f, color = android.graphics.Color.parseColor("#999999"))
        paint.color = android.graphics.Color.parseColor("#999999")
        paint.textSize = 24f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("₹${String.format(Locale.US, "%.2f", b.taxShare + b.tipShare)}", width - margin, y, paint)
        y += smallLineHeight + 8f

        drawDivider()
        y += lineHeight * 0.4f
    }

    // Grand Total
    drawText("GRAND TOTAL", margin, bold = true, size = 36f, color = android.graphics.Color.parseColor("#1A1A1A"))
    paint.color = android.graphics.Color.parseColor("#6200EE")
    paint.textSize = 40f
    paint.isFakeBoldText = true
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("₹${String.format(Locale.US, "%.2f", grandTotalSum)}", width - margin, y, paint)

    return bitmap
}

@Composable
fun ReceiptSummaryScreen(
    uiState: SplitterUiState,
    viewModel: SplitterViewModel
) {
    val context = LocalContext.current
    val breakdowns = remember(uiState) { viewModel.calculateBreakdown() }
    val grandTotalSum = remember(breakdowns) { breakdowns.sumOf { it.grandTotal } }

    val onShareBill = {
        try {
            val bitmap = buildReceiptBitmap(uiState, breakdowns, grandTotalSum)
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "splixter_bill.png")
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Bill Receipt Image"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val onSharePdf = {
        try {
            val bitmap = buildReceiptBitmap(uiState, breakdowns, grandTotalSum)
            val pdfDocument = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val cachePath = File(context.cacheDir, "documents")
            cachePath.mkdirs()
            val file = File(cachePath, "splixter_bill.pdf")
            FileOutputStream(file).use { stream ->
                pdfDocument.writeTo(stream)
            }
            pdfDocument.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Bill PDF Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp)
            ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                IconButton(onClick = { viewModel.setStep(AppStep.ASSIGN) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Final Split Bill",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ready to share with the group",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Receipt Card + Buttons
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                FullExportReceiptCard(
                    uiState = uiState,
                    breakdowns = breakdowns,
                    grandTotalSum = grandTotalSum
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Share Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onShareBill() },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share Image", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Image",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onSharePdf() },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = "Export PDF", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export PDF",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.clearAllData() },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(text = "New Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            com.example.splixter.ui.components.ConfettiEffect()
        }
    }
}
}

@Composable
fun FullExportReceiptCard(
    uiState: SplitterUiState,
    breakdowns: List<PersonBreakdown>,
    grandTotalSum: Double
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFDF7) // Soft thermal receipt parchment
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0DBCF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            val paidByPerson = uiState.people.find { it.id == uiState.paidByPersonId }
            val paidByName = paidByPerson?.let { "${it.emoji} ${it.name}" } ?: "N/A"

            Text(
                text = "SPLIXTER RECEIPT",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF2C2C2C),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bill Paid by: $paidByName",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFCCCCCC), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                breakdowns.forEach { b ->
                    PersonReceiptBlock(breakdown = b)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFCCCCCC), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GRAND TOTAL",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "₹${String.format(Locale.US, "%.2f", grandTotalSum)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PersonReceiptBlock(breakdown: PersonBreakdown) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = breakdown.person.emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = breakdown.person.name.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF222222)
                )
            }
            Text(
                text = "₹${String.format(Locale.US, "%.2f", breakdown.grandTotal)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF111111)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        for ((item, costShare) in breakdown.items) {
            val catEmoji = if (item.category == com.example.splixter.data.ItemCategory.LIQUOR) "🍺" else "🍕"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$catEmoji ${item.name}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color(0xFF555555)
                )
                Text(
                    text = "₹${String.format(Locale.US, "%.2f", costShare)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color(0xFF555555)
                )
            }
        }

        val totalDiscShare = breakdown.discountShare
        if (totalDiscShare > 0.001) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "- Discount",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFF22A85A)
                )
                Text(
                    text = "-₹${String.format(Locale.US, "%.2f", totalDiscShare)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFF22A85A)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "+ Tax & Tip Share",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFF475569)
            )
            Text(
                text = "₹${String.format(Locale.US, "%.2f", breakdown.taxShare + breakdown.tipShare)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFF475569)
            )
        }
    }
}
