package com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.databinding.DialogManuelEntryBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel

class ManualEntryDialogFragment(
    private val activityId : String,
    private val attendanceId : String,
    private val allParticipants : List<Participant>,
    //private val onParticipantSelected: (String) -> Unit
) : DialogFragment() {
    private val mainViewModel : MainViewModel by viewModels()
    private var _binding: DialogManuelEntryBinding? = null
    private val binding get() = _binding!!
    private var selectedParticipantId : Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        _binding = DialogManuelEntryBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setDimAmount(0.6f)
        }

        loadParticipantsToAdapter()

        // dropdown item select
        binding.manuelNameDropdown.setOnItemClickListener { parent, view, position, id ->
            val selectedParticipant = parent.getItemAtPosition(position) as Participant
            selectedParticipantId = selectedParticipant.id


        }

        //dialog fragment add participant
        binding.buttonAddParticipant.setOnClickListener {
            selectedParticipantId?.let {
                mainViewModel.approveParticipant(activityId,attendanceId, selectedParticipantId!!)
            } ?: Toast.makeText(requireContext(),"Kullanıcı id si alınamaı!", Toast.LENGTH_SHORT).show()

        }

        return dialog
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
