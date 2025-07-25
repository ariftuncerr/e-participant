package com.vedatturkkal.stajokulu2025yoklama.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vedatturkkal.stajokulu2025yoklama.R
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance

class AttendanceListAdapter (private val context: android.content.Context, private val attendanceList: List<ParticipantAttendance>) : RecyclerView.Adapter<AttendanceListAdapter.AttendanceItemHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttendanceItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance,parent,false)
        return AttendanceItemHolder(view)
    }

    override fun onBindViewHolder(
        holder: AttendanceItemHolder,
        position: Int
    ) {
        holder.pAttendName.text = attendanceList[position].participant.name.toString()
        holder.pCheckInTime.text = attendanceList[position].checkInTime
        holder.pCheckOutTime.text = attendanceList[position].checkOutTime
        holder.pApproval.text = attendanceList[position].approval.toString()

    }

    override fun getItemCount(): Int {
        return attendanceList.size
    }

    class AttendanceItemHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val pAttendCard = itemView.findViewById<CardView>(R.id.pAttendCard)
        val pAttendName = itemView.findViewById<TextView>(R.id.pAttendText)
        val pCheckInTime = itemView.findViewById<TextView>(R.id.checkInTime)
        val pCheckOutTime = itemView.findViewById<TextView>(R.id.checkOutTimeTxt)
        val pApproval = itemView.findViewById<TextView>(R.id.approvalTxt)
    }
}