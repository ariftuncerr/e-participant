package com.vedatturkkal.stajokulu2025yoklama.ui.main.home

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentAddParticipantDialogBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class AddParticipantDialogFragment(private val activityId: String) : DialogFragment() {

    private lateinit var binding: FragmentAddParticipantDialogBinding
    private val mainViewModel: MainViewModel by viewModels({ requireParentFragment() })

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = FragmentAddParticipantDialogBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)

        binding.addParticipantButton.setOnClickListener {
            val name = binding.participantNameEditText.text.toString()
            if (name.isNotBlank()) {
                lifecycleScope.launch {
                    mainViewModel.addParticipant(activityId, name)
                }
                dismiss()
            } else {
                Snackbar.make(binding.root, "Katılımcı adı boş olamaz!", Snackbar.LENGTH_SHORT).show()
            }
        }

        return builder.create()
    }
}
