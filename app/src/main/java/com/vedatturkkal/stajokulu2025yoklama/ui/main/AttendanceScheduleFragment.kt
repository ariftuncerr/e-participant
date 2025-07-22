package com.vedatturkkal.stajokulu2025yoklama.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vedatturkkal.stajokulu2025yoklama.R

// Etkinlik Seçimi Takvim seçimi yapılır -> günler listelenir
// Günlerin altında giriş ve çıkış saatleri yanda yer alan kullanıcı yoklamaları oluşturulur
// firebase den bilgiyi çeker.

class AttendanceScheduleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendance_schedule, container, false)
    }

}