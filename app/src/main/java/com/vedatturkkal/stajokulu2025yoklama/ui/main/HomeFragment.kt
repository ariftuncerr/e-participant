package com.vedatturkkal.stajokulu2025yoklama.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vedatturkkal.stajokulu2025yoklama.R

//Kullanıcı ismi Etkinlik Adı yer alacak fragment
//Kullancı bir etkinlik oluşturur Staj Okulu 2025 gibi
//Etkinliğe ait kullanıcılar kaydeder. (NFC isim okuma ya da manuel)

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

}