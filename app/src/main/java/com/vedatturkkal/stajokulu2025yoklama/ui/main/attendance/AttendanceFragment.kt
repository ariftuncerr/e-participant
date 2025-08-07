// AttendanceFragment.kt
package com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance

import ParticipantAttendanceAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.vedatturkkal.stajokulu2025yoklama.data.model.Activity
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.data.repo.ParticipantAttendanceRepository
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentAttendanceBinding
import com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance.attendanceDialog.AddAttendanceDialogFragment
import com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance.methodDialog.IdentityVerificationDialogFragment
import com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance.methodDialog.ManualEntryDialogFragment
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.ParticipantAttendanceViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.fragment.app.viewModels

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    private var currentAttendanceId: String? = null
    private var selectedActivity: Activity? = null
    private var participantList: List<Participant> = emptyList()

    // Hilt yok: custom factory ile
    private val attendanceVM: ParticipantAttendanceViewModel by viewModels {
        PAViewModelFactory(ParticipantAttendanceRepository())
    }

    private var selectedMethod: String? = null

    private lateinit var adapter: ParticipantAttendanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        val view: View = binding.root

        adapter = ParticipantAttendanceAdapter(
            onApprove = { pid -> withIds { aId, attId -> attendanceVM.approve(aId, attId, pid) } },
            onUnapprove = { pid -> withIds { aId, attId -> attendanceVM.unapprove(aId, attId, pid) } },
            onReject = { pid -> withIds { aId, attId -> attendanceVM.reject(aId, attId, pid) } },
            onUnreject = { pid -> withIds { aId, attId -> attendanceVM.unreject(aId, attId, pid) } }
        )
        binding.attendanceParticipantsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.attendanceParticipantsRecyclerView.adapter = adapter

        // Yoklama başlat
        binding.startAttendanceBtn.setOnClickListener {
            if (selectedActivity == null) {
                Toast.makeText(requireContext(), "Lütfen bir aktivite seçin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AddAttendanceDialogFragment(selectedActivity!!.id).show(parentFragmentManager, "AddAttendanceDialog")
        }

        // Dialog dönüşü: attendanceId geldiğinde önce herkesi ekle sonra canlı dinlemeyi başlat
        setFragmentResultListener("addAttendanceResultKey") { _, bundle ->
            val attId = bundle.getString("attendanceId") ?: return@setFragmentResultListener
            currentAttendanceId = attId

            withIds { aId, _ ->
                lifecycleScope.launch {
                    val ok = attendanceVM.addAll(aId, attId)  // <-- Boolean döndürüyor
                    if (ok) {
                        Snackbar.make(binding.root, "Tüm katılımcılar yoklamaya eklendi", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(binding.root, "Katılımcılar eklenemedi!", Snackbar.LENGTH_SHORT).show()
                    }
                    attendanceVM.startListening(aId, attId)
                }
            }
        }

        setupActivityDropdown()
        observeViewModel()
        setupMethodClickListeners()

        // Add All / Remove All
        binding.addAll.setOnClickListener {
            withIds { aId, attId ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val ok = attendanceVM.addAll(aId, attId)
                    Snackbar.make(
                        binding.root,
                        if (ok) "Tüm katılımcılar yoklamaya eklendi" else "Ekleme başarısız!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // Dinleme başlamadıysa güvence olsun:
                    attendanceVM.startListening(aId, attId)
                }
            }
        }
        binding.removeAll.setOnClickListener {
            withIds { aId, attId ->
                attendanceVM.removeAll(aId, attId)
                Snackbar.make(binding.root, "Yoklamadan tüm katılımcılar kaldırıldı", Snackbar.LENGTH_SHORT).show()
                // Canlı dinleme açıksa, liste otomatik boşalır. Değilse:
                // adapter.submitList(emptyList())
            }
        }

        return view
    }

    private inline fun withIds(block: (activityId: String, attendanceId: String) -> Unit) {
        val aId = selectedActivity?.id
        val attId = currentAttendanceId
        if (aId == null || attId == null) {
            Toast.makeText(requireContext(), "Lütfen önce yoklama başlatın", Toast.LENGTH_SHORT).show()
            return
        }
        block(aId, attId)
    }

    private fun setupMethodClickListeners() {
        binding.manuelMethod.setOnClickListener { openMethodDialog("Manuel") }
        binding.idCheckMethod.setOnClickListener { openMethodDialog("Kimlik ile") }
        binding.nfcCheckMethod.setOnClickListener { openMethodDialog("NFC") }
        binding.cardCheckMethod.setOnClickListener { openMethodDialog("Kart") }
        binding.qrMethod.setOnClickListener { openMethodDialog("QR") }
    }

    private fun openMethodDialog(method: String) {
        if (selectedActivity == null || currentAttendanceId == null) {
            Toast.makeText(requireContext(), "Lütfen önce yoklama başlatın", Toast.LENGTH_SHORT).show()
            return
        }
        when (method) {
            "Manuel" -> ManualEntryDialogFragment(
                selectedActivity!!.id, currentAttendanceId!!, participantList
            ).show(parentFragmentManager, "ManualEntryDialog")

            "Kimlik ile" -> IdentityVerificationDialogFragment(
                selectedActivity!!.id, currentAttendanceId!!, participantList
            ).show(parentFragmentManager, "IdentityVerificationDialog")
        }
    }

    private fun getAllParticipants() {
        selectedActivity?.let { viewModel.getParticipants(it.id) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.participantList.collectLatest { list -> participantList = list }
        }
    }

    private fun setupActivityDropdown() {
        viewModel.getActivities()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activitiesResult.collectLatest { activityList ->
                val activityNames = activityList.map { it.title }
                val ddAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    activityNames
                )
                binding.activitiesDropdown.setAdapter(ddAdapter)
                binding.activitiesDropdown.setOnItemClickListener { _, _, position, _ ->
                    selectedActivity = activityList[position]
                    getAllParticipants()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            attendanceVM.items.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Custom Factory (Hilt yokken şart) ---
    private class PAViewModelFactory(
        private val repo: ParticipantAttendanceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ParticipantAttendanceViewModel::class.java)) {
                return ParticipantAttendanceViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
