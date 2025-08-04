package com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.databinding.DialogIdentityMethodBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import java.util.concurrent.Executors

class IdentityVerificationDialogFragment(
    private val activityId: String,
    private val attendanceId: String,
    private val allParticipants: List<Participant>
) : DialogFragment() {

    private var _binding: DialogIdentityMethodBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var alreadyApprovedIds = mutableSetOf<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogIdentityMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setDimAmount(0.6f)
        }

        observeViewModel()

        binding.btnStartCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    simulateReadId(imageProxy)?.let { detectedId ->
                        val participant = allParticipants.find { it.id.toString() == detectedId }
                        if (participant != null && !alreadyApprovedIds.contains(participant.id)) {
                            alreadyApprovedIds.add(participant.id)
                            requireActivity().runOnUiThread {
                                showSuccessIcon()
                                mainViewModel.approveParticipant(activityId, attendanceId, participant.id)
                            }
                        }
                    }
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, analyzer)

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun simulateReadId(imageProxy: ImageProxy): String? {
        // Örnek okuma — burayı OCR veya QR kod okuma ile değiştirebilirsin
        return listOf("1", "2", "3", "999").random() // 999 = geçersiz örnek
    }

    private fun showSuccessIcon() {
        binding.imgSuccessIcon.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.imgSuccessIcon.visibility = View.GONE
        }, 2000)
    }

    private fun observeViewModel() {
        mainViewModel.addParticipantResult.observe(viewLifecycleOwner) { isAdded ->
            if (isAdded) {
                Toast.makeText(requireContext(), "Kimlik doğrulandı ve eklendi.", Toast.LENGTH_SHORT).show()
                // dismiss() istenirse buraya
            } else {
                Toast.makeText(requireContext(), "Ekleme başarısız.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Kamera izni reddedildi.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
