package com.vedatturkkal.stajokulu2025yoklama.ui.main.home

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.vedatturkkal.stajokulu2025yoklama.R
import com.vedatturkkal.stajokulu2025yoklama.data.model.Activity
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentAddActivityDialogBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class AddActivityDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentAddActivityDialogBinding

    private val mainViewModel: MainViewModel by viewModels({ requireParentFragment() })

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = FragmentAddActivityDialogBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)

        binding.addActivityButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            if (title.isNotBlank()) {
                val activity = Activity(title = title)
                lifecycleScope.launch {
                    mainViewModel.createActivity(activity)
                }
                dismiss() // Başarılıysa dialog kapat
            } else {
                Snackbar.make(binding.root, "Aktivite Başlığı Girmelisin!", Snackbar.LENGTH_SHORT).show()
            }
        }

        return builder.create()
    }
}
