package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.core.BengaliFormat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class ReportSection(
    val headingBn: String,
    val rows: List<List<String>>,
    val arabicLine: String? = null
)

object BengaliReportExporter {

    fun generateBengaliPdf(
        context: Context,
        fileNamePrefix: String,
        titleBn: String,
        subtitleBn: String,
        sections: List<ReportSection>
    ): Result<File> {
        return try {
            val dir = File(context.cacheDir, "reports").apply { mkdirs() }
            val outFile = File(dir, "${fileNamePrefix}_${System.currentTimeMillis()}.pdf")

            val bengaliTypeface = ResourcesCompat.getFont(context, R.font.noto_sans_bengali) ?: Typeface.DEFAULT
            val arabicTypeface = ResourcesCompat.getFont(context, R.font.noto_naskh_arabic) ?: Typeface.DEFAULT

            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            val margin = 40f
            val contentWidth = (pageWidth - margin * 2).toInt()

            val pdf = PdfDocument()
            var pageNum = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            var page = pdf.startPage(pageInfo)
            var canvas = page.canvas

            val headerPaint = Paint().apply {
                color = Color.parseColor("#0B4F3A")
                style = Paint.Style.FILL
            }
            val goldPaint = Paint().apply {
                color = Color.parseColor("#C9972C")
                strokeWidth = 2.5f
            }
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(bengaliTypeface, Typeface.BOLD)
                textSize = 15f
                color = Color.WHITE
            }
            val subHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bengaliTypeface
                textSize = 10.5f
                color = Color.parseColor("#E6F4EE")
            }
            val headingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(bengaliTypeface, Typeface.BOLD)
                textSize = 12.5f
                color = Color.parseColor("#0B4F3A")
            }
            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bengaliTypeface
                textSize = 10.5f
                color = Color.parseColor("#1A2B23")
            }
            val arabicPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = arabicTypeface
                textSize = 14f
                color = Color.parseColor("#0B4F3A")
            }

            fun drawHeader(c: Canvas): Float {
                c.drawRect(0f, 0f, pageWidth.toFloat(), 92f, headerPaint)
                c.drawLine(0f, 92f, pageWidth.toFloat(), 92f, goldPaint)

                val madrasaTitle = "খেলার মাঠ নূরানী তালিমুল কুরআন ও হাফিজিয়া মাদরাসা (প্রতিষ্ঠিত: ১৯৯৫)"
                val mLayout = StaticLayout.Builder.obtain(madrasaTitle, 0, madrasaTitle.length, titlePaint, contentWidth).build()
                c.save()
                c.translate(margin, 16f)
                mLayout.draw(c)
                c.restore()

                val sub = "$titleBn  |  $subtitleBn  |  তারিখ: ${BengaliFormat.formatDateBn(BengaliFormat.todayDateKey())}"
                val sLayout = StaticLayout.Builder.obtain(sub, 0, sub.length, subHeaderPaint, contentWidth).build()
                c.save()
                c.translate(margin, 54f)
                sLayout.draw(c)
                c.restore()
                return 112f
            }

            var y = drawHeader(canvas)

            fun ensureSpace(neededHeight: Float) {
                if (y + neededHeight > pageHeight - 48f) {
                    pdf.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                    page = pdf.startPage(pageInfo)
                    canvas = page.canvas
                    y = drawHeader(canvas)
                }
            }

            for (section in sections) {
                val hLayout = StaticLayout.Builder.obtain(section.headingBn, 0, section.headingBn.length, headingPaint, contentWidth).build()
                ensureSpace(hLayout.height + 16f)
                canvas.save()
                canvas.translate(margin, y)
                hLayout.draw(canvas)
                canvas.restore()
                y += hLayout.height + 6f

                if (!section.arabicLine.isNullOrBlank()) {
                    // Arabic RTL text rendered right-aligned with Noto Naskh Arabic
                    val aText = section.arabicLine
                    val aLayout = StaticLayout.Builder.obtain(aText, 0, aText.length, arabicPaint, contentWidth)
                        .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
                        .build()
                    ensureSpace(aLayout.height + 10f)
                    canvas.save()
                    canvas.translate(margin, y)
                    aLayout.draw(canvas)
                    canvas.restore()
                    y += aLayout.height + 8f
                }

                for (row in section.rows) {
                    val line = row.joinToString("  •  ")
                    val rLayout = StaticLayout.Builder.obtain(line, 0, line.length, bodyPaint, contentWidth).build()
                    ensureSpace(rLayout.height + 6f)
                    canvas.save()
                    canvas.translate(margin, y)
                    rLayout.draw(canvas)
                    canvas.restore()
                    y += rLayout.height + 5f
                }
                y += 12f
            }

            pdf.finishPage(page)
            FileOutputStream(outFile).use { pdf.writeTo(it) }
            pdf.close()
            Result.success(outFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generateBengaliExcelCsv(
        context: Context,
        fileNamePrefix: String,
        headers: List<String>,
        rows: List<List<String>>
    ): Result<File> {
        return try {
            val dir = File(context.cacheDir, "reports").apply { mkdirs() }
            val outFile = File(dir, "${fileNamePrefix}_${System.currentTimeMillis()}.csv")
            val sb = StringBuilder()
            // UTF-8 BOM so Microsoft Excel & Google Sheets open Bengali Unicode cleanly
            sb.append('\uFEFF')
            fun escapeCsv(cell: String): String = "\"${cell.replace("\"", "\"\"")}\""
            sb.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")
            for (row in rows) {
                sb.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
            }
            FileOutputStream(outFile).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            Result.success(outFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String, chooserTitleBn: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitleBn))
    }

    fun printPdfFile(context: Context, pdfFile: File, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination?.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, adapter, null)
    }
}
