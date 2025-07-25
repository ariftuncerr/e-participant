package com.vedatturkkal.stajokulu2025yoklama.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vedatturkkal.stajokulu2025yoklama.R

// Kamera ile kart isim bilgisi alınarak kullanıcının yoklaması alındığı kısım.
// Kullanıcı ismi kameradan dönecek. İsim boş değilse sistem saati ui tarafında görüncek.
// tarih + saat
class AttendanceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendance, container, false)
    }

}