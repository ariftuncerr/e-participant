package com.vedatturkkal.stajokulu2025yoklama.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vedatturkkal.stajokulu2025yoklama.R
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant

class ParticipantListAdapter (
    context: android.content.Context,
    private var participantList: List<Participant>,
    private val onDeleteClick: (Participant) -> Unit
): RecyclerView.Adapter<ParticipantListAdapter.ParticipantItemHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ParticipantItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_participant,parent,false)
        return ParticipantItemHolder(view)
    }

    override fun onBindViewHolder(
        holder: ParticipantItemHolder,
        position: Int
    ) {
        holder.participantName.text = participantList[position].name
        val participant = participantList[position]
        // seçilen katılımcıyı listeden siler
        holder.deleteParticipantBtn.setOnClickListener {
            onDeleteClick(participant)
        }
    }

    override fun getItemCount(): Int {
        return participantList.size
    }

    class ParticipantItemHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val participantName = itemView.findViewById<TextView>(R.id.participantNameText)
        val deleteParticipantBtn = itemView.findViewById<ImageView>(R.id.deleteParticipantBtn)
    }

     fun updateList(newList : List<Participant>){
        participantList = newList
        notifyDataSetChanged()

    }
}