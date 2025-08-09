package com.vedatturkkal.stajokulu2025yoklama.utils.export

import android.content.Context
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

object ExcelExportUtil {

    /**
     * Android'de AWT yok; bu nedenle POI'nin autoSizeColumn'ı kullanmıyoruz.
     * Kolon genişliklerini ELLE ayarlıyoruz (karakter uzunluğu * 256).
     */
    fun export(
        context: Context,
        rows: List<ParticipantAttendance>,
        activityTitle: String,
        attendanceTitle: String,
        attendanceTime: String
    ): File {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("Attendance")

        // ---------- Styles ----------
        val headerFont: XSSFFont = wb.createFont().apply {
            fontHeightInPoints = 12
            bold = true
        }
        val headerStyle: CellStyle = wb.createCellStyle().apply {
            setFont(headerFont)
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            wrapText = false
        }

        val metaFont: XSSFFont = wb.createFont().apply {
            fontHeightInPoints = 11
            bold = true
        }
        val metaStyle: CellStyle = wb.createCellStyle().apply {
            setFont(metaFont)
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
        }

        var cellStyle: CellStyle = wb.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
            wrapText = false
        }

        // ---------- Meta (üst bilgi) ----------
        var r = 0
        sheet.createRow(r).apply {
            createCell(0).apply { setCellValue("Activity"); cellStyle = metaStyle }
            createCell(1).apply { setCellValue(activityTitle); cellStyle = cellStyle }
        }
        r++

        sheet.createRow(r).apply {
            createCell(0).apply { setCellValue("Attendance"); cellStyle = metaStyle }
            createCell(1).apply { setCellValue(attendanceTitle); cellStyle = cellStyle }
        }
        r++

        sheet.createRow(r).apply {
            createCell(0).apply { setCellValue("Time"); cellStyle = metaStyle }
            createCell(1).apply { setCellValue(attendanceTime); cellStyle = cellStyle }
        }
        r += 2 // 1 boş satır

        // ---------- Header ----------
        val headerRow = sheet.createRow(r)
        fun setHeader(col: Int, text: String) {
            headerRow.createCell(col).apply {
                setCellValue(text)
                cellStyle = headerStyle
            }
        }

        // Sütun isimleri
        setHeader(0, "#")
        setHeader(1, "Katılımcı")
        setHeader(2, "Durum")
        // Örnek ek sütunlar istersek (yorumdan çıkar, aşağıda veri yazımını da ekle):
        // setHeader(3, "Giriş Saati")
        // setHeader(4, "Çıkış Saati")

        r++

        // ---------- Data ----------
        var max0 = "#".length
        var max1 = "Katılımcı".length
        var max2 = "Durum".length
        // var max3 = "Giriş Saati".length
        // var max4 = "Çıkış Saati".length

        rows.forEachIndexed { index, pa ->
            val row = sheet.createRow(r)

            val num = (index + 1).toString()
            val name = pa.participant.name
            val status = when {
                pa.approval -> "Onaylı"
                pa.denied -> "Reddedildi"
                else -> "—"
            }

            row.createCell(0).apply { setCellValue(num); cellStyle = cellStyle }
            row.createCell(1).apply { setCellValue(name); cellStyle = cellStyle }
            row.createCell(2).apply { setCellValue(status); cellStyle = cellStyle }

            // Eğer modelde varsa:
            // val checkIn = pa.checkInTimeText ?: ""
            // val checkOut = pa.checkOutTimeText ?: ""
            // row.createCell(3).apply { setCellValue(checkIn); cellStyle = cellStyle }
            // row.createCell(4).apply { setCellValue(checkOut); cellStyle = cellStyle }

            max0 = maxOf(max0, num.length)
            max1 = maxOf(max1, name.length)
            max2 = maxOf(max2, status.length)
            // max3 = maxOf(max3, checkIn.length)
            // max4 = maxOf(max4, checkOut.length)

            r++
        }

        // ---------- Kolon genişlikleri (manuel) ----------
        fun setWidth(col: Int, charCount: Int, padding: Int = 2) {
            val width = min(255, charCount + padding) * 256
            sheet.setColumnWidth(col, width)
        }
        setWidth(0, max0)
        setWidth(1, max1)
        setWidth(2, max2)
        // setWidth(3, max3)
        // setWidth(4, max4)

        // ---------- Dosyaya yaz ----------
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val safeActivity = activityTitle.ifBlank { "activity" }.replace("[^a-zA-Z0-9-_]".toRegex(), "_")
        val safeAttendance = attendanceTitle.ifBlank { "attendance" }.replace("[^a-zA-Z0-9-_]".toRegex(), "_")
        val fileName = "attendance_${safeActivity}_${safeAttendance}_$ts.xlsx"
        val outFile = File(context.cacheDir, fileName)

        FileOutputStream(outFile).use { fos -> wb.write(fos) }
        wb.close()

        return outFile
    }
}
