package com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.databinding.DialogManuelEntryBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel

class ManualEntryDialogFragment(
    private val activityId: String,
    private val attendanceId: String,
    private val allParticipants: List<Participant>,
) : DialogFragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private var _binding: DialogManuelEntryBinding? = null
    private val binding get() = _binding!!
    private var selectedParticipantId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogManuelEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setDimAmount(0.6f)
        }

        observeViewModel()
        loadParticipantsToAdapter()

        // dropdown item select
        binding.manuelNameDropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedParticipant = parent.getItemAtPosition(position) as Participant
            selectedParticipantId = selectedParticipant.id
        }

        // add button click
        binding.buttonAddParticipant.setOnClickListener {
            selectedParticipantId?.let {
                mainViewModel.approveParticipant(activityId, attendanceId, it)
            } ?: Toast.makeText(requireContext(), "Kullanıcı id si alınamadı!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        mainViewModel.addParticipantResult.observe(viewLifecycleOwner) { isAdded ->
            if (isAdded) {
                Toast.makeText(requireContext(), "Katılımcı eklendi.", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Katılımcı eklenemedi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadParticipantsToAdapter() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            allParticipants
        )
        binding.manuelNameDropdown.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
