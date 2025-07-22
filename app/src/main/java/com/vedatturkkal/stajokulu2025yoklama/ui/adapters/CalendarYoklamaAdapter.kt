package com.vedatturkkal.stajokulu2025yoklama.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vedatturkkal.stajokulu2025yoklama.R

class CalendarYoklamaAdapter(private val yoklamaList: List<CalendarYoklamaData>) :
    RecyclerView.Adapter<CalendarYoklamaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val isimTextView: TextView = view.findViewById(R.id.isimTextView)
        val saatTextView: TextView = view.findViewById(R.id.saatTextView)
        val durumTextView: TextView = view.findViewById(R.id.durumTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_yoklama, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val yoklama = yoklamaList[position]

        holder.isimTextView.text = yoklama.isim
        holder.saatTextView.text = if (yoklama.saat.isNotEmpty()) yoklama.saat else "-"
        holder.durumTextView.text = yoklama.durum

        // Duruma göre renk ayarla
        when (yoklama.durum) {
            "Geldi" -> {
                holder.durumTextView.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
                )
            }
            "Gelmedi" -> {
                holder.durumTextView.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
                )
            }
            "Geç Geldi" -> {
                holder.durumTextView.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark)
                )
            }
        }
    }

    override fun getItemCount() = yoklamaList.size
}

// Takvim için ayrı data class
data class CalendarYoklamaData(
    val isim: String,
    val saat: String,
    val durum: String
)