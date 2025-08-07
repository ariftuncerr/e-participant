package com.vedatturkkal.stajokulu2025yoklama.utils.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportUtil {

    fun export(
        context: Context,
        rows: List<ParticipantAttendance>,
        activityTitle: String,
        attendanceTitle: String,
        attendanceTime: String
    ): File {
        val doc = PdfDocument()
        val paint = Paint().apply { textSize = 12f }
        val titlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true }

        val pageWidth = 595  // A4 width px @72dpi
        val pageHeight = 842 // A4 height
        val left = 40f
        val topStart = 60f
        val lineH = 18f
        val maxLinesPerPage = ((pageHeight - topStart - 40) / lineH).toInt()

        var pageIndex = 1
        var lineIndex = 0
        var y = topStart

        fun newPage(): PdfDocument.Page {
            val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex++).create()
            val page = doc.startPage(info)
            y = topStart
            lineIndex = 0

            page.canvas.drawText("Aktivite: $activityTitle", left, y, titlePaint); y += lineH
            page.canvas.drawText("Yoklama: $attendanceTitle  •  Saat: $attendanceTime", left, y, paint); y += (lineH * 1.5f)

            // Header
            page.canvas.drawText("Katılımcı", left, y, titlePaint)
            page.canvas.drawText("Durum", pageWidth - 140f, y, titlePaint)
            y += (lineH)

            return page
        }

        var page = newPage()

        rows.forEach { pa ->
            if (lineIndex >= maxLinesPerPage) {
                doc.finishPage(page)
                page = newPage()
            }
            val status = when {
                pa.approval -> "Onaylı"
                pa.denied -> "Reddedildi"
                else -> "—"
            }
            page.canvas.drawText(pa.participant.name, left, y, paint)
            page.canvas.drawText(status, pageWidth - 140f, y, paint)
            y += lineH
            lineIndex++
        }

        doc.finishPage(page)

        val ts = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "yoklama_$ts.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
}