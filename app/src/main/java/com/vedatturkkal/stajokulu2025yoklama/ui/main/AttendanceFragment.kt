package com.vedatturkkal.stajokulu2025yoklama.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentAttendanceBinding
import java.util.concurrent.atomic.AtomicBoolean

class AttendanceFragment : Fragment() {
    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val isProcessing = AtomicBoolean(false)

    private lateinit var cameraProvider: ProcessCameraProvider
    private var recognizedName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)

        binding.startAttendanceBtn.setOnClickListener {
            checkPermissionAndStartCamera()
        }

        return binding.root
    }

    private fun checkPermissionAndStartCamera() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissions(arrayOf(permission), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Kamera izni gerekli", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.cameraView.surfaceProvider)
        }

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analyzer.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
            processImageProxy(imageProxy)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, analyzer)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Kamera başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
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
                val result = extractNameAndDetailsFromText(visionText.text)
                if (!result.isNullOrBlank()) {
                    if (isProcessing.compareAndSet(false, true)) {
                        recognizedName = result
                        binding.participantName.text = result
                        binding.doneScan.visibility = View.VISIBLE
                        cameraProvider.unbindAll()
                        Toast.makeText(requireContext(), "Katılımcı: $result", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Tanıma hatası", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun extractNameAndDetailsFromText(text: String): String? {
        val lines = text.lines()
        var surname: String? = null
        var name: String? = null

        lines.forEachIndexed { i, line ->
            val cleanLine = line.trim()

            if (cleanLine.contains("Soyadı", true) || cleanLine.contains("Surname", true)) {
                surname = lines.getOrNull(i + 1)?.trim().takeIf { !it.isNullOrBlank() }
                    ?: cleanLine.split(":").getOrNull(1)?.trim()
            }

            if (cleanLine.contains("Adı", true) || cleanLine.contains("Given Name", true)) {
                name = lines.getOrNull(i + 1)?.trim().takeIf { !it.isNullOrBlank() }
                    ?: cleanLine.split(":").getOrNull(1)?.trim()
            }
        }

        surname = surname?.replace(Regex("[^a-zA-ZğüşıöçĞÜŞİÖÇ\\s]"), "")?.trim()
        name = name?.replace(Regex("[^a-zA-ZğüşıöçĞÜŞİÖÇ\\s]"), "")?.trim()

        return if (!surname.isNullOrBlank() && !name.isNullOrBlank() &&
            isValidName(name) && isValidName(surname)) {
            "$name $surname"
        } else null
    }

    private fun isValidName(name: String): Boolean {
        if (name.length < 2) return false
        val suspiciousPatterns = listOf(
            Regex("[0-9]"),
            Regex("[^a-zA-ZğüşıöçĞÜŞİÖÇ\\s]"),
            Regex(".*[aeiouıAEIOUI]{4,}.*"),
            Regex(".*[bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ]{5,}.*"),
            Regex(".*[XQW].*", RegexOption.IGNORE_CASE),
            Regex(".*[0O]{2,}.*"),
            Regex(".*[Il1]{3,}.*")
        )
        return suspiciousPatterns.none { it.matches(name) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        recognizer.close()
    }
}
