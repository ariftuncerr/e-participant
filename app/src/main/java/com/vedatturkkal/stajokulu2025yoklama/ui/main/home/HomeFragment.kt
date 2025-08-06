package com.vedatturkkal.stajokulu2025yoklama.ui.main.home

import ExcelImportParticipantDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.vedatturkkal.stajokulu2025yoklama.data.model.Activity
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentHomeBinding
import com.vedatturkkal.stajokulu2025yoklama.ui.adapters.ParticipantListAdapter
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val mainViewModel: MainViewModel by viewModels()
    private var selectedActivity: Activity? = null
    private var userActivityList: List<Activity> = emptyList()
    private lateinit var participantListAdapter: ParticipantListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 🟢 Aktivite Ekle Butonu
        binding.addActiviyBtn.setOnClickListener {
            AddActivityDialogFragment().show(childFragmentManager, "AddActivityDialog")
        }

        // 🟢 Aktivite seçilince katılımcı listesi yüklenir
        (binding.activitiesDropdown as AutoCompleteTextView).setOnItemClickListener { _, _, position, _ ->
            selectedActivity = userActivityList[position]
            selectedActivity?.let {
                mainViewModel.getParticipants(it.id)
            }
        }

        // 🟢 Katılımcı Ekleme Kartına Tıklama
        binding.cardView.setOnClickListener {
            selectedActivity?.let {
                AddParticipantDialogFragment(it.id).show(childFragmentManager, "AddParticipantDialog")
            } ?: Snackbar.make(binding.root, "Lütfen önce bir aktivite seç!", Snackbar.LENGTH_SHORT).show()
        }
        binding.importExceImageView.setOnClickListener {
            selectedActivity?.let {
                ExcelImportParticipantDialogFragment(it.id).show(childFragmentManager, "ImportExcelDialog")
            } ?: Snackbar.make(binding.root, "Lütfen önce bir aktivite seç!", Snackbar.LENGTH_SHORT).show()
        }


        observeViewModel()
        mainViewModel.getActivities()

        return binding.root
    }

    private fun observeViewModel() {
        // ✅ Aktivite Ekleme Sonucu
        lifecycleScope.launch {
            mainViewModel.addActivityResult.observe(viewLifecycleOwner) { success ->
                if (success) {
                    Snackbar.make(binding.root, "Aktivite başarıyla eklendi", Snackbar.LENGTH_SHORT).show()
                    mainViewModel.getActivities()
                } else {
                    Snackbar.make(binding.root, "Aktivite eklenirken hata oluştu", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        // ✅ Kullanıcı Aktiviteleri
        lifecycleScope.launch {
            mainViewModel.activitiesResult.collectLatest { activities ->
                userActivityList = activities
                setUpUserActivities(activities)
            }
        }

        // ✅ Katılımcı Ekleme Sonucu
        lifecycleScope.launch {
            mainViewModel.addParticipantResult.observe(viewLifecycleOwner) { success ->
                Snackbar.make(binding.root, "Katılımcı başarıyla eklendi", Snackbar.LENGTH_SHORT).show()
            }
        }

        // ✅ Katılımcı Listesi
        lifecycleScope.launch {
            mainViewModel.participantList.collectLatest { participants ->
                if (::participantListAdapter.isInitialized) {
                    updateParticipantList(participants)
                } else {
                    showParticipants(participants)
                }
            }
        }
    }

    private fun setUpUserActivities(activities: List<Activity>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            activities.map { it.title }
        )
        (binding.activitiesDropdown as AutoCompleteTextView).setAdapter(adapter)
    }

    private fun showParticipants(participants: List<Participant>) {
        participantListAdapter = ParticipantListAdapter(requireContext(), participants) { participant ->
            selectedActivity?.let {
                mainViewModel.deleteParticipant(it.id, participant.id)
            }
        }

        binding.participantsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.participantsRecyclerView.adapter = participantListAdapter
    }

    private fun updateParticipantList(participants: List<Participant>) {
        participantListAdapter.updateList(participants)
    }
}
