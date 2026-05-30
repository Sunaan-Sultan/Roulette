package com.project.roulette.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.project.roulette.domain.model.SpinResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.FileOutputStream
import android.net.Uri
import com.project.roulette.presentation.viewmodel.PdfStats
import java.util.Locale

object PdfExporter {

    fun generateAndSharePdf(context: Context, wheelName: String, results: List<SpinResult>, stats: PdfStats): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Colors
        val primaryColor = Color.rgb(108, 92, 231) // Matches theme purple
        val headerColor = Color.rgb(45, 52, 54)
        val rowColor1 = Color.WHITE
        val rowColor2 = Color.rgb(240, 240, 240)

        // 1. Header
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SPIN HISTORY REPORT", 40f, 45f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Wheel: $wheelName", 40f, 75f, paint)

        // 2. Summary Stats Section
        var y = 140f
        paint.color = headerColor
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SUMMARY STATS", 40f, y, paint)
        
        y += 30f
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        // Stat boxes/text
        val summaryX = floatArrayOf(40f, 160f, 320f)
        canvas.drawText("Total Spins: ${stats.totalSpins}", summaryX[0], y, paint)
        canvas.drawText("Avg Duration: ${String.format(Locale.US, "%.1fs", stats.avgDuration)}", summaryX[1], y, paint)
        canvas.drawText("Most Picked: ${stats.mostPicked ?: "N/A"}", summaryX[2], y, paint)

        // 3. Win Distribution Section
        y += 50f
        paint.color = headerColor
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("WIN DISTRIBUTION", 40f, y, paint)

        y += 25f
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        stats.distribution.take(5).forEach { item ->
            paint.color = Color.BLACK
            canvas.drawText("${item.name}: ${item.count} picks (${item.percentage}%)", 40f, y, paint)
            
            // Draw a small bar
            val barWidth = (item.percentage / 100f) * 200f
            paint.color = primaryColor
            canvas.drawRect(250f, y - 10f, 250f + barWidth, y, paint)
            
            y += 20f
        }

        // 4. Spin History Table Headers
        y += 30f
        paint.color = headerColor
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RECENT SPINS", 40f, y, paint)

        y += 25f
        val xCols = floatArrayOf(40f, 100f, 280f, 380f, 460f)
        val headers = arrayOf("Spin #", "Winner", "Duration", "Angle", "Time")

        paint.textSize = 12f
        headers.forEachIndexed { i, text ->
            canvas.drawText(text, xCols[i], y, paint)
        }

        y += 10f
        paint.strokeWidth = 2f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 25f

        // 5. Table Rows
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f

        results.reversed().forEachIndexed { index, result ->
            if (index % 2 == 0) paint.color = rowColor1 else paint.color = rowColor2
            canvas.drawRect(35f, y - 18f, 560f, y + 8f, paint)

            paint.color = Color.BLACK
            val dt = result.spinTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
            val dateStr = String.format(Locale.US, "%02d:%02d:%02d", dt.hour, dt.minute, dt.second)

            canvas.drawText("${index + 1}", xCols[0], y, paint)
            canvas.drawText(result.selectedSegmentName, xCols[1], y, paint)
            canvas.drawText(String.format(Locale.US, "%.1fs", result.spinDuration / 1000f), xCols[2], y, paint)
            canvas.drawText(String.format(Locale.US, "%.1f°", result.finalAngle), xCols[3], y, paint)
            canvas.drawText(dateStr, xCols[4], y, paint)

            y += 26f
            if (y > 800) return@forEachIndexed 
        }

        pdfDocument.finishPage(page)

        // Save to cache
        val fileName = "History_${wheelName.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, fileName)
        
        return try {
            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            pdfDocument.close()
            null
        }
    }
}
