package com.vedatturkkal.stajokulu2025yoklama.utils.export

import android.content.Context
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExportUtil {

    fun export(
        context: Context,
        rows: List<ParticipantAttendance>,
        activityTitle: String,
        attendanceTitle: String,
        attendanceTime: String
    ): File {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("Yoklama")

        // Header
        val head = sheet.createRow(0)
        head.createCell(0).setCellValue("Aktivite")
        head.createCell(1).setCellValue("Yoklama")
        head.createCell(2).setCellValue("Saat")
        head.createCell(3).setCellValue("Katılımcı")
        head.createCell(4).setCellValue("Durum")

        val center: CellStyle = wb.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER // istersen LEFT/RIGHT
        }

        var r = 1
        rows.forEach { pa ->
            val row = sheet.createRow(r++)

            row.createCell(0).apply { setCellValue(activityTitle);    setCellStyle(center) }
            row.createCell(1).apply { setCellValue(attendanceTitle);  setCellStyle(center) }
            row.createCell(2).apply { setCellValue(attendanceTime);   setCellStyle(center) }
            row.createCell(3).apply { setCellValue(pa.participant.name); setCellStyle(center) }

            val status = when {
                pa.approval -> "Onaylı"
                pa.denied   -> "Reddedildi"
                else        -> "—"
            }
            row.createCell(4).apply { setCellValue(status); setCellStyle(center) }
        }

        // Otomatik kolon genişliği
        (0..4).forEach { sheet.autoSizeColumn(it) }

        val ts = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "yoklama_$ts.xlsx")
        FileOutputStream(file).use { wb.write(it) }
        wb.close()
        return file
    }
}