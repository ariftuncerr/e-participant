package com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance.attendanceDialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentAddAttendanceDialogBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddAttendanceDialogFragment(
    private val activityId: String
) : DialogFragment() {

    private lateinit var binding: FragmentAddAttendanceDialogBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentAddAttendanceDialogBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(false)

        // Varsayılan tarih
        val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        binding.attendanceDateTimeEditText.setText(currentDateTime)

        // Tarih ve saat seçim alanı
        binding.attendanceDateTimeEditText.setOnClickListener {
            showDateTimePicker()
        }

        // Kaydet butonu
        binding.addAttendanceButton.setOnClickListener {
            val title = binding.attendanceTitleEditText.text.toString().trim()
            val date = binding.attendanceDateTimeEditText.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen başlık girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Attendance oluştur
            viewModel.addAttendance(activityId, date, title)

            // Sonucu dinle
            viewModel.addAttendanceResult.observe(this) { result ->
                val (success, attendanceId) = result
                if (success && attendanceId != null) {

                    // Fragment'e attendanceId döndür
                    val resultBundle = Bundle().apply {
                        putString("attendanceId", attendanceId)
                    }
                    setFragmentResult("addAttendanceResultKey", resultBundle)

                    Toast.makeText(requireContext(), "Yoklama Başlatıldı", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Yoklama başlatılamadı", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return builder.create()
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val selectedDateTime = String.format(
                    "%04d-%02d-%02d %02d:%02d",
                    year, month + 1, day, hour, minute
                )
                binding.attendanceDateTimeEditText.setText(selectedDateTime)
            },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
