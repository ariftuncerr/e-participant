package com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentAttendanceScheduleBinding
import com.vedatturkkal.stajokulu2025yoklama.ui.adapters.AttendanceScheduleAdapter
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.AttendanceScheduleViewModel
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.vedatturkkal.stajokulu2025yoklama.utils.export.PdfExportUtil
import com.vedatturkkal.stajokulu2025yoklama.utils.export.ExcelExportUtil
import com.vedatturkkal.stajokulu2025yoklama.utils.export.ExportUtils

class AttendanceScheduleFragment : Fragment() {

    private var _binding: FragmentAttendanceScheduleBinding? = null
    private val binding get() = _binding!!

    private val mainVM: MainViewModel by viewModels()
    private val scheduleVM: AttendanceScheduleViewModel by viewModels()

    private lateinit var adapter: AttendanceScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceScheduleBinding.inflate(inflater, container, false)

        setupRecycler()
        setupActivityDropdown()
        setupDatePicker()
        setupAttendanceDropdown()
        setupExports()
        observeFlows()

        return binding.root
    }

    private fun setupRecycler() {
        adapter = AttendanceScheduleAdapter(
            onApprove = { pid -> scheduleVM.approve(pid) },
            onReject  = { pid -> scheduleVM.reject(pid) }
        )
        binding.scheduleRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.scheduleRecycler.adapter = adapter
    }

    private fun setupActivityDropdown() {
        mainVM.getActivities()
        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.activitiesResult.collectLatest { list ->
                scheduleVM.setActivities(list)
                val names = list.map { it.title }
                val dd = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
                binding.activitiesDropdown.setAdapter(dd)
                binding.activitiesDropdown.setOnItemClickListener { _, _, pos, _ ->
                    scheduleVM.onActivitySelected(list[pos])
                    binding.dateInput.setText("")
                    binding.attendanceDropdown.setText("")
                }
            }
        }
    }

    private fun setupDatePicker() {
        binding.dateInputLayout.setEndIconOnClickListener { openDatePicker() }
        binding.dateInput.setOnClickListener { openDatePicker() }
    }

    private fun openDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Yoklama tarihi seç")
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis ->
            binding.dateInput.setText(picker.headerText)
            scheduleVM.onDateSelected(utcMillis)
            binding.attendanceDropdown.setText("")
        }
        picker.show(parentFragmentManager, "datePicker")
    }

    private fun setupAttendanceDropdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleVM.attendancesOfDate.collectLatest { attendances ->
                val labels = attendances.map { "${it.timeText} • ${it.title}" }
                val dd = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, labels)
                binding.attendanceDropdown.setAdapter(dd)

                if (attendances.isEmpty()) binding.attendanceDropdown.setText("")

                binding.attendanceDropdown.setOnItemClickListener { _, _, pos, _ ->
                    scheduleVM.onAttendanceSelected(attendances[pos].id)
                }
            }
        }
    }

    private fun setupExports() {
        binding.exportExcelCard.setOnClickListener {
            val list = scheduleVM.items.value
            if (list.isEmpty()) {
                Snackbar.make(binding.root, "Listede veri yok", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val activityTitle = scheduleVM.selectedActivity.value?.title ?: "-"
            val att = scheduleVM.attendancesOfDate.value
                .find { it.id == scheduleVM.selectedAttendanceId.value }
            val attendanceTitle = att?.title ?: "-"
            val attendanceTime = att?.timeText ?: "-"

            val file = ExcelExportUtil.export(requireContext(), list, activityTitle, attendanceTitle, attendanceTime)

            // ✅ Doğru MIME ve başlık
            ExportUtils.shareFile(
                requireContext(),
                file,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "Excel çıktısını paylaş"
            )
        }

        binding.exportPdfCard.setOnClickListener {
            val list = scheduleVM.items.value
            if (list.isEmpty()) {
                Snackbar.make(binding.root, "Listede veri yok", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val activityTitle = scheduleVM.selectedActivity.value?.title ?: "-"
            val att = scheduleVM.attendancesOfDate.value
                .find { it.id == scheduleVM.selectedAttendanceId.value }
            val attendanceTitle = att?.title ?: "-"
            val attendanceTime = att?.timeText ?: "-"

            val file = PdfExportUtil.export(requireContext(), list, activityTitle, attendanceTitle, attendanceTime)
            ExportUtils.shareFile(requireContext(), file, "application/pdf", "PDF çıktısını paylaş")
        }
    }

    private fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleVM.items.collectLatest { list -> adapter.submitList(list) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
