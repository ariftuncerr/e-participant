package com.vedatturkkal.stajokulu2025yoklama.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.vedatturkkal.stajokulu2025yoklama.R
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance

class AttendanceScheduleAdapter(
    private val onApprove: (participantId: Int) -> Unit,
    private val onReject: (participantId: Int) -> Unit
) : ListAdapter<ParticipantAttendance, AttendanceScheduleAdapter.VH>(DiffCb) {

    object DiffCb : DiffUtil.ItemCallback<ParticipantAttendance>() {
        override fun areItemsTheSame(oldItem: ParticipantAttendance, newItem: ParticipantAttendance): Boolean {
            return oldItem.participant.id == newItem.participant.id
        }

        override fun areContentsTheSame(oldItem: ParticipantAttendance, newItem: ParticipantAttendance): Boolean {
            return oldItem == newItem
        }
    }

    class VH(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        val textName: TextView = card.findViewById(R.id.textName)
        val textStatus: TextView = card.findViewById(R.id.textStatus)
        val btnApprove: MaterialButton = card.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = card.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_schedule, parent, false) as MaterialCardView
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.textName.text = item.participant.name.ifBlank { "—" }

        // İki-durumlu gösterim
        val isApproved = item.approval
        val isDenied = item.denied

        when {
            isApproved -> {
                holder.textStatus.text = "✔ Onaylı"
                holder.btnApprove.isEnabled = false
                holder.btnReject.isEnabled = true
            }
            isDenied -> {
                holder.textStatus.text = "🚫 Reddedildi"
                holder.btnApprove.isEnabled = true
                holder.btnReject.isEnabled = false
            }
            else -> {
                holder.textStatus.text = "—"
                holder.btnApprove.isEnabled = true
                holder.btnReject.isEnabled = true
            }
        }

        holder.btnApprove.setOnClickListener {
            onApprove(item.participant.id)
        }
        holder.btnReject.setOnClickListener {
            onReject(item.participant.id)
        }
    }
}
