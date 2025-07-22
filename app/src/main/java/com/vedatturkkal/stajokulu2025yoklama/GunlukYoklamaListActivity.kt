package com.vedatturkkal.stajokulu2025yoklama

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vedatturkkal.stajokulu2025yoklama.ui.adapters.CalendarYoklamaAdapter
import com.vedatturkkal.stajokulu2025yoklama.ui.adapters.CalendarYoklamaData

class GunlukYoklamaListActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CalendarYoklamaAdapter
    private var selectedDate: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gunluk_yoklama_list)

        // Intent'ten tarihi al
        selectedDate = intent.getStringExtra("selectedDate") ?: ""

        titleTextView = findViewById(R.id.titleTextView)
        recyclerView = findViewById(R.id.recyclerView)

        // Tarihi başlıkta göster
        titleTextView.text = "$selectedDate Tarihli Yoklama Listesi"

        // RecyclerView setup
        recyclerView.layoutManager = LinearLayoutManager(this)

        // O günün yoklama verilerini yükle
        loadYoklamaDataForDate(selectedDate)
    }

    private fun loadYoklamaDataForDate(date: String) {
        val yoklamaList = getYoklamaDataForDate(date)

        adapter = CalendarYoklamaAdapter(yoklamaList)
        recyclerView.adapter = adapter
    }


    private fun getYoklamaDataForDate(date: String): List<CalendarYoklamaData> {
        // Burada gerçek veritabanı sorgusunu yapacaksınız
        // Örnek olarak bazı test verileri dönüyorum

        // Eğer bugünün tarihi ise örnek veri göster
        val today = java.time.LocalDate.now().toString()
        return if (date == today) {
            listOf(
                CalendarYoklamaData("Ahmet Yılmaz", "09:15", "Geldi"),
                CalendarYoklamaData("Ayşe Kaya", "09:20", "Geldi"),
                CalendarYoklamaData("Mehmet Özkan", "", "Gelmedi")
            )
        } else {
            // Diğer günler için boş liste
            emptyList()
        }
    }
}

// Yoklama verileri için data class
data class YoklamaData(
    val isim: String,
    val saat: String,
    val durum: String
)