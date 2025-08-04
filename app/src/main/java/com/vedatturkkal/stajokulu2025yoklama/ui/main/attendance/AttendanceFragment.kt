package com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.vedatturkkal.stajokulu2025yoklama.data.model.Activity
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentAttendanceBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var currentAttendanceId: String? = null
    private var selectedActivity: Activity? = null
    private var participantList : List<Participant> = emptyList()

    private var selectedMethod : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        val view : View = binding.root

        setupActivitySpinner()
        setupMethodSpinner()
        observeViewModel()

        binding.startAttendanceBtn.setOnClickListener { view ->
            takeAttendanceClick(view)
        }

        binding.createAttendanceBtn.setOnClickListener { view ->
            addAttendance()
        }

        return view


    }
    //clickable functions
    private fun takeAttendanceClick (view : View){

        if (selectedMethod == "Manuel" && selectedActivity != null && currentAttendanceId != null) {
            val dialog = ManualEntryDialogFragment(selectedActivity!!.id,currentAttendanceId!!,participantList)

            dialog.show(parentFragmentManager, "ManualEntryDialog")
        }
        else if (selectedMethod == "Kimlik ile" && selectedActivity != null && currentAttendanceId != null) {
            val dialog = IdentityVerificationDialogFragment(selectedActivity!!.id,currentAttendanceId!!,participantList)

            dialog.show(parentFragmentManager, "IdentifyVerificationDialog")
        }
        else Snackbar.make(binding.root,"Aktivite ve Method seçimi Zorunlu!", Snackbar.LENGTH_SHORT).show()
    }


    private fun getAllParticipants(){
        selectedActivity?.let {
            viewModel.getParticipants(it.id)
        }
        lifecycleScope.launch {
            viewModel.participantList.collectLatest { list ->
                participantList = list
                println(participantList)
            }
        }
    }

    private fun setupActivitySpinner() {
        viewModel.getActivities()
        lifecycleScope.launch {
            viewModel.activitiesResult.collectLatest { activityList ->
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    activityList
                )

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.activitiesSpinner.adapter = adapter

                binding.activitiesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedActivity = activityList.getOrNull(position)

                        getAllParticipants()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        selectedActivity = null
                    }
                }
            }
        }
    }

    private fun addAttendance() {
        if (selectedActivity == null) {
            Toast.makeText(requireContext(), "Lütfen bir aktivite seçin", Toast.LENGTH_SHORT).show()
        }
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModel.addAttendance(selectedActivity!!.id, currentDate)

    }

    private fun setupMethodSpinner() {
        val methods = resources.getStringArray(com.vedatturkkal.stajokulu2025yoklama.R.array.attendance_methods)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, methods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.methodsSpinner.adapter = adapter

        binding.methodsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMethod = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun observeViewModel() {
        // add Attendance
        viewModel.addAttendanceResult.observe (viewLifecycleOwner) { result ->
            val (success,currentId) = result
            if(success){
                Snackbar.make(binding.root,"Yoklama Oluşturuldu", Snackbar.LENGTH_SHORT).show()
                currentId.let {
                    currentAttendanceId = it
                }
                viewModel.addAllPtToAttendance(selectedActivity!!.id,currentAttendanceId!!)

                println(currentAttendanceId)
            }
            else{
                Toast.makeText(requireContext(),"Yoklama başlatılırken Hata", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addAllResult.observe (viewLifecycleOwner) { success ->
            if (success)
                Snackbar.make(binding.root,"Tüm katılımcılar Yoklama Listesine Eklendi", Snackbar.LENGTH_SHORT).show()
            else
                Snackbar.make(binding.root,"Tüm katılımcılar Yoklama Listesine Eklenemedi!", Snackbar.LENGTH_SHORT).show()

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
