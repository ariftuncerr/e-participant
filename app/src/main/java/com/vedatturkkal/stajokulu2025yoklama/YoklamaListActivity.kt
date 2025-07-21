package com.vedatturkkal.stajokulu2025yoklama

import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class YoklamaListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var stajyerDate: TextView
    private lateinit var stajyerTotalCount: TextView
    private lateinit var adapter: YoklamaAdapter
    private var attendeesList = mutableListOf<AttendeeItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yoklama_list)

        initViews()
        setupRecyclerView()
        loadYoklamaData()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewYoklama)
        stajyerDate = findViewById(R.id.stajyerDate)
        stajyerTotalCount = findViewById(R.id.stajyerTotalCount)

        // Bugünün tarihini göster
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr", "TR"))
        stajyerDate.text = today.format(formatter)
    }

    private fun setupRecyclerView() {
        adapter = YoklamaAdapter(mutableListOf()) { attendeeItem, position ->
            showDeleteConfirmationDialog(attendeeItem, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun showDeleteConfirmationDialog(attendeeItem: AttendeeItem, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Kaydı Sil")
            .setMessage("${attendeeItem.fullName} adlı kişinin yoklama kaydını silmek istediğinizden emin misiniz?")
            .setPositiveButton("Sil") { _: DialogInterface, _: Int ->
                deleteAttendeeRecord(attendeeItem, position)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deleteAttendeeRecord(attendeeItem: AttendeeItem, position: Int) {
        try {
            // Listeden kaldır
            attendeesList.removeAt(position)

            // Adapter güncelle
            adapter.removeItem(position)

            // Paylaşılan tercihleri güncelle
            saveUpdatedYoklamaData()

            // Sayıyı güncelle
            updateTotalCount()

            Toast.makeText(this, "${attendeeItem.fullName} kaydı silindi", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Kayıt silinirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUpdatedYoklamaData() {
        val sharedPref = getSharedPreferences("YoklamaPrefs", MODE_PRIVATE)
        val today = LocalDate.now().toString()

        val yoklamaData = if (attendeesList.isEmpty()) {
            ""
        } else {
            attendeesList.joinToString("\n") { "${it.fullName}|${it.timeString}" }
        }

        sharedPref.edit()
            .putString("yoklama_$today", yoklamaData)
            .apply()
    }

    private fun updateTotalCount() {
        stajyerTotalCount.text = if (attendeesList.isEmpty()) {
            "Bugün henüz yoklama alınmamış"
        } else {
            "Toplam: ${attendeesList.size} kişi"
        }
    }

    private fun loadYoklamaData() {
        try {
            val sharedPref = getSharedPreferences("YoklamaPrefs", MODE_PRIVATE)
            val today = LocalDate.now().toString()
            val yoklamaData = sharedPref.getString("yoklama_$today", "")

            if (yoklamaData.isNullOrEmpty()) {
                showEmptyState()
                return
            }

            attendeesList.clear()
            attendeesList.addAll(
                yoklamaData.split("\n")
                    .filter { it.isNotBlank() }
                    .mapIndexed { index, line ->
                        val parts = line.trim().split("|")
                        val name = parts[0].trim()
                        val timeString = if (parts.size > 1) parts[1].trim() else "00:00"

                        AttendeeItem(
                            id = index + 1,
                            fullName = name,
                            timeString = timeString
                        )
                    }
            )

            adapter.updateList(attendeesList)
            updateTotalCount()

        } catch (e: Exception) {
            Toast.makeText(this, "Veriler yüklenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            showEmptyState()
        }
    }

    private fun showEmptyState() {
        attendeesList.clear()
        adapter.updateList(mutableListOf())
        stajyerTotalCount.text = "Bugün henüz yoklama alınmamış!"
    }
}

// Data class
data class AttendeeItem(
    val id: Int,
    val fullName: String,
    val timeString: String
)
