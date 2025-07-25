package com.vedatturkkal.stajokulu2025yoklama.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentAttendanceBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val isProcessing = AtomicBoolean(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(requireContext(), "Kamera izni gerekli", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                processImageProxy(imageProxy)
            }

            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(viewLifecycleOwner, selector, preview, analyzer)

            updateStatus("Kimlik kartınızı kameraya gösterin")

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        if (isProcessing.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                handleTextRecognitionResult(visionText.text)
            }
            .addOnFailureListener {
                updateStatus("Metin tanıma hatası, tekrar deneyin...")
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun handleTextRecognitionResult(text: String) {
        if (text.isBlank()) return

        val lines = text.lines()
        var name: String? = null
        var surname: String? = null

        lines.forEachIndexed { i, line ->
            val l = line.trim()
            if (l.contains("Adı", true)) name = lines.getOrNull(i + 1)?.trim()
            if (l.contains("Soyadı", true)) surname = lines.getOrNull(i + 1)?.trim()
        }

        if (!name.isNullOrBlank() && !surname.isNullOrBlank()) {
            val fullName = "$name $surname"
            if (isProcessing.compareAndSet(false, true)) {
                saveAttendance(fullName)
            }
        }
    }

    private fun saveAttendance(fullName: String) {
        showProcessing(true)
        updateStatus("İşleniyor...")

        val prefs = requireContext().getSharedPreferences("YoklamaPrefs", 0)
        val today = LocalDate.now().toString()
        val oldData = prefs.getString("yoklama_$today", "") ?: ""

        if (oldData.contains(fullName)) {
            Toast.makeText(requireContext(), "$fullName zaten kaydedilmiş!", Toast.LENGTH_SHORT).show()
            resetProcess()
            return
        }

        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val newEntry = if (oldData.isEmpty()) "$fullName|$now" else "$oldData\n$fullName|$now"

        prefs.edit().putString("yoklama_$today", newEntry).apply()

        Toast.makeText(requireContext(), "$fullName başarıyla kaydedildi!", Toast.LENGTH_LONG).show()
        resetProcess()
    }

    private fun resetProcess() {
        isProcessing.set(false)
        showProcessing(false)
        updateStatus("Kimlik kartınızı kameraya gösterin")
    }

    private fun showProcessing(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateStatus(msg: String) {
        binding.stajyerStatus.apply {
            text = msg
            visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recognizer.close()
        _binding = null
    }
}
