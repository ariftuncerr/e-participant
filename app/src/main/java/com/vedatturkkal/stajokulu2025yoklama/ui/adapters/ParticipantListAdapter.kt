package com.vedatturkkal.stajokulu2025yoklama.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vedatturkkal.stajokulu2025yoklama.R
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant

class ParticipantListAdapter(
    context: android.content.Context,
    private var participantList: List<Participant>,
    private val onDeleteClick: (Participant) -> Unit
) : RecyclerView.Adapter<ParticipantListAdapter.ParticipantItemHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ParticipantItemHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return ParticipantItemHolder(view)
    }

    override fun onBindViewHolder(
        holder: ParticipantItemHolder,
        position: Int
    ) {
        val participant = participantList[position]

        // 🟢 Pozisyona göre 1'den başlayan numara veriliyor
        holder.participantNo.text = (position + 1).toString()
        holder.participantName.text = participant.name

        holder.deleteParticipantBtn.setOnClickListener {
            onDeleteClick(participant)
        }
    }

    override fun getItemCount(): Int {
        return participantList.size
    }

    class ParticipantItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val participantNo = itemView.findViewById<TextView>(R.id.participantNo)
        val participantName = itemView.findViewById<TextView>(R.id.participantNameText)
        val deleteParticipantBtn = itemView.findViewById<ImageView>(R.id.deleteParticipantBtn)
    }

    fun updateList(newList: List<Participant>) {
        participantList = newList
        notifyDataSetChanged()
    }
}
