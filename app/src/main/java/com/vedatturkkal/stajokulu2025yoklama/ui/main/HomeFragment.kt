package com.vedatturkkal.stajokulu2025yoklama.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import com.vedatturkkal.stajokulu2025yoklama.R
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentHomeBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.vedatturkkal.stajokulu2025yoklama.data.model.Activity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

        binding.addActiviyBtn.setOnClickListener { view ->
            if (binding.addActivityCard.isVisible) binding.addActivityCard.visibility = View.GONE else binding.addActivityCard.visibility = View.VISIBLE

            binding.addActivityButton.setOnClickListener { view ->
                val activityTitle = binding.addActivityEditText.text.toString()
                if (activityTitle.isNotEmpty()){
                    val activity = Activity(title = activityTitle)
                    lifecycleScope.launch {
                        mainViewModel.createActivity(activity)
                    }
                    binding.addActivityCard.visibility = View.GONE
                }
                else
                    Snackbar.make(view,"Aktivite Başlığı Girmelisin!!", Snackbar.LENGTH_SHORT).show()

            }


        }
        observeViewModel()

        mainViewModel.getActivities()
        return binding.root
    }
    private fun observeViewModel(){
        lifecycleScope.launch {
            mainViewModel.addActivityResult.observe (viewLifecycleOwner) { success ->
                if (success)
                    Snackbar.make(binding.root,"Aktivite Başarıyla Eklendi", Snackbar.LENGTH_SHORT).show()
                else
                    Snackbar.make(binding.root,"Aktivite eklenirken Hata Oluştu", Snackbar.LENGTH_SHORT).show()

            }
        }
        lifecycleScope.launch {
            mainViewModel.activitiesResult.collectLatest { userActivitiesList ->
                setUpUserActivities(userActivitiesList)

            }
        }

    }
    private fun setUpUserActivities(activitiesList : List<Activity>){
        var activityNames = mutableListOf<String>()
        for (activity in activitiesList){
            activityNames.add(activity.title)
            println(activity.title)
        }

        val dropDownAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,android.R.id.text1,activityNames)
       ( binding.activitiesDropdown as AutoCompleteTextView).setAdapter(dropDownAdapter)

    }

}