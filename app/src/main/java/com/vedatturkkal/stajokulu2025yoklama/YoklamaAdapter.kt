package com.vedatturkkal.stajokulu2025yoklama

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class YoklamaAdapter(
    private var yoklamaList: MutableList<AttendeeItem>,
    private val onItemDelete: ((AttendeeItem, Int) -> Unit)? = null
) : RecyclerView.Adapter<YoklamaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val isimTextView: TextView = view.findViewById(R.id.nameTxtView)
        val saatTextView: TextView = view.findViewById(R.id.checkInTime)
        val durumTextView: TextView = view.findViewById(R.id.statusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val yoklama = yoklamaList[position]

        holder.isimTextView.text = yoklama.fullName
        holder.saatTextView.text = if (yoklama.timeString.isNotEmpty()) yoklama.timeString else "-"

        // Burada durum bilgisini koyacak uygun bir alan yoksa "-" koyabilirsin
        holder.durumTextView.text = "-"

        // Silme işlemi için uzun tıklama
        holder.itemView.setOnLongClickListener {
            onItemDelete?.invoke(yoklama, position)
            true
        }
    }

    override fun getItemCount() = yoklamaList.size

    fun updateList(newList: MutableList<AttendeeItem>) {
        yoklamaList.clear()
        yoklamaList.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in yoklamaList.indices) {
            yoklamaList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}