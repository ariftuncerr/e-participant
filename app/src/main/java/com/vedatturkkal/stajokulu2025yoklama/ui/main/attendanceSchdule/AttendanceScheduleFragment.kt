package com.vedatturkkal.stajokulu2025yoklama.ui.main.attendanceSchdule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentAttendanceScheduleBinding
import com.vedatturkkal.stajokulu2025yoklama.ui.adapters.AttendanceListAdapter

class AttendanceScheduleFragment : Fragment() {
    private lateinit var binding: FragmentAttendanceScheduleBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAttendanceScheduleBinding.inflate(inflater,container,false)
        setUpRecyclerView()
        return binding.root
    }

    private fun setUpRecyclerView(){

        val p1 = Participant(uuid = "123", name = "Vedat")
        val p2 = Participant(uuid = "234", name = "Arif")
        val p3 = Participant(uuid = "567", name = "Ahmet")

        val pA1 = ParticipantAttendance(
            participant = p1,
            checkInTime = "09.10",
            checkOutTime = "16.00",
            approval = true
        )
        val pA2 = ParticipantAttendance(
            participant = p2,
            checkInTime = "09.11",
            checkOutTime = "16.01",
            approval = true
        )
        val pA3 = ParticipantAttendance(
            participant = p3,
            checkInTime = "09.12",
            checkOutTime = "16.02",
            approval = true
        )

        val exampleAttList = listOf<ParticipantAttendance>(pA1,pA2,pA3)
        val adapter = AttendanceListAdapter(requireContext(), exampleAttList)

        binding.approvalPartipiciantList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.approvalPartipiciantList.adapter = adapter
    }


}