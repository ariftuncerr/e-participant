package com.vedatturkkal.stajokulu2025yoklama.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.vedatturkkal.stajokulu2025yoklama.R
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentHomeBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel

//Kullanıcı ismi Etkinlik Adı yer alacak fragment
//Kullancı bir etkinlik oluşturur Staj Okulu 2025 gibi
//Etkinliğe ait kullanıcılar kaydeder. (NFC isim okuma ya da manuel)

class HomeFragment : Fragment() {
    private lateinit var binding : FragmentHomeBinding
    private val mainViewModel : MainViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)

        observeViewModel()


        return binding.root
    }
    private fun observeViewModel(){

    }

}