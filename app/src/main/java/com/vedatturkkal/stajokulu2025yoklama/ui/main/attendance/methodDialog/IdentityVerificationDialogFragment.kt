package com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance.methodDialog

import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import android.view.*
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.google.android.gms.tasks.Tasks
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.databinding.DialogIdentityMethodBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import java.text.Normalizer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

class IdentityVerificationDialogFragment(
    private val activityId: String,
    private val attendanceId: String,
    private val participants: List<Participant>,
    private val autoResumeAfterSuccess: Boolean = false // 5 sn sonra kamera yeniden başlasın mı?
) : DialogFragment() {

    companion object {
        private const val REQ_CAMERA = 9001
        const val RESULT_KEY = "IDENTITY_APPROVED_RESULT"
        const val RESULT_PARTICIPANT_ID = "participantId"
        const val RESULT_PARTICIPANT_NAME = "participantName"
    }

    private var _binding: DialogIdentityMethodBinding? = null
    private val binding get() = _binding!!

    private val vm: MainViewModel by activityViewModels()

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null

    private var running = false
    private var pausedForApproval = false

    private var lastName: String? = null
    private var lastAt = 0L
    private val debounceMs = 900L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogIdentityMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cameraExecutor = Executors.newSingleThreadExecutor()

        // ÖNEMLİ: TextureView moduna al → overlay görünür.
        binding.previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        binding.previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

        // Overlay her zaman en önde olsun
        binding.approvalOverlay.apply {
            isClickable = false
            isFocusable = false
            bringToFront()
            elevation = 9999f
            visibility = View.GONE
        }

        binding.btnStartCamera.setOnClickListener { startFlow() }
        binding.btnStopCamera.setOnClickListener { stopFlow() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (running && !pausedForApproval) startCamera()
    }

    override fun onStop() {
        super.onStop()
        teardownCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        teardownCamera()
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
        _binding = null
    }

    // -------- Flow --------
    private fun startFlow() {
        if (running) return
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQ_CAMERA)
            return
        }
        running = true
        pausedForApproval = false
        startCamera()
    }

    private fun stopFlow() {
        running = false
        pausedForApproval = false
        teardownCamera()
        safeUi {
            binding.tvDetectedName.text = "Okunan isim burada görünecek"
            binding.approvalOverlay.visibility = View.GONE
            binding.imgSuccessIcon.visibility = View.GONE
            binding.tvApprovedMessage.text = ""
        }
    }

    // -------- CameraX --------
    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(requireContext())
        future.addListener({
            cameraProvider = future.get()
            bindUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return
        try { provider.unbindAll() } catch (_: Exception) {}

        previewUseCase = Preview.Builder().build().also {
            safeUi { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
        }

        analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(cameraExecutor) { imageProxy ->
                    try {
                        if (running && !pausedForApproval) processFrame(imageProxy)
                    } catch (_: Exception) {
                    } finally {
                        imageProxy.close()
                    }
                }
            }

        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            camera = provider.bindToLifecycle(this, selector, previewUseCase, analysisUseCase)
            enablePinchToZoom(camera!!, binding.previewView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun teardownCamera() {
        try { cameraProvider?.unbindAll() } catch (_: Exception) {}
    }

    // -------- Analyzer --------
    private fun processFrame(imageProxy: ImageProxy) {
        val raw = extractNameFromId(imageProxy) ?: return

        val now = SystemClock.uptimeMillis()
        if (raw.equals(lastName, ignoreCase = true) && now - lastAt < debounceMs) return
        lastName = raw
        lastAt = now

        val norm = normalizeName(raw)
        val match = findMatch(norm, participants)

        safeUi { binding.tvDetectedName.text = raw }

        if (match != null) {
            // 1) Firestore/Repo üzerinden onayla
            vm.approveParticipant(activityId, attendanceId, match.id)

            // 2) Anında UI güncellemesi için result yolla
            setFragmentResult(
                RESULT_KEY,
                bundleOf(
                    RESULT_PARTICIPANT_ID to match.id,
                    RESULT_PARTICIPANT_NAME to (match.name ?: raw)
                )
            )

            // 3) 5 sn overlay
            pauseWithOverlay(match.name ?: raw)
        }
    }

    private fun pauseWithOverlay(displayName: String) {
        pausedForApproval = true
        teardownCamera() // Kamera DURUR

        safeUi {
            binding.tvDetectedName.text = displayName
            binding.tvApprovedMessage.text = "Onaylandı: $displayName"
            binding.imgSuccessIcon.visibility = View.VISIBLE

            // Overlay'i öne çek & görünür yap
            binding.approvalOverlay.apply {
                bringToFront()
                elevation = 9999f
                visibility = View.VISIBLE
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            safeUi {
                binding.approvalOverlay.visibility = View.GONE
                binding.imgSuccessIcon.visibility = View.GONE
                binding.tvApprovedMessage.text = ""
            }
            if (running) {
                pausedForApproval = false
                if (autoResumeAfterSuccess) startCamera()
            }
        }, 5000)
    }

    // İstersen bilgi diyalogu göstermek için (şu an kullanılmıyor)
    @Suppress("unused")
    private fun showApprovedAlert(name: String) {
        if (!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Onaylandı")
            .setMessage("$name yoklaması alındı.")
            .setPositiveButton("Tamam", null)
            .show()
    }

    // -------- OCR: Ad + Soyad -------------------------
    private fun extractNameFromId(imageProxy: ImageProxy): String? {
        val mediaImage = imageProxy.image ?: return null
        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result: Text = try {
            Tasks.await(recognizer.process(image), 1500, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            return null
        }

        val lines = mutableListOf<String>()
        for (block in result.textBlocks) {
            for (line in block.lines) {
                val t = line.text.trim()
                if (t.isNotEmpty()) lines.add(t)
            }
        }
        if (lines.isEmpty()) return null

        fun norm(s: String): String {
            val mapTR = mapOf(
                'ç' to 'c','ğ' to 'g','ı' to 'i','ö' to 'o','ş' to 's','ü' to 'u',
                'Ç' to 'c','Ğ' to 'g','İ' to 'i','I' to 'i','Ö' to 'o','Ş' to 's','Ü' to 'u'
            )
            val replaced = s.map { mapTR[it] ?: it }.joinToString("")
            val noMarks = Normalizer.normalize(replaced, Normalizer.Form.NFD)
                .replace("\\p{Mn}+".toRegex(), "")
            return noMarks.lowercase().replace(Regex("\\s+"), " ").trim()
        }

        val surnameKeys = listOf("soyadi", "soyadı", "surname")
        val nameKeys = listOf("adi", "adı", "given name", "given names", "given name(s)", "given names(s)")

        var foundName: String? = null
        var foundSurname: String? = null

        fun pickValueFrom(labelLine: String): String? {
            val idx = labelLine.indexOf(':')
            return if (idx >= 0 && idx + 1 < labelLine.length) {
                labelLine.substring(idx + 1).trim()
            } else null
        }

        for (i in lines.indices) {
            val raw = lines[i]
            val n = norm(raw)
            if (surnameKeys.any { n.contains(it) }) {
                foundSurname = pickValueFrom(raw)?.takeIf { it.isNotBlank() }
                    ?: lines.getOrNull(i + 1)?.takeIf { it.isNotBlank() }
            }
            if (nameKeys.any { n.contains(it) }) {
                foundName = pickValueFrom(raw)?.takeIf { it.isNotBlank() }
                    ?: lines.getOrNull(i + 1)?.takeIf { it.isNotBlank() }
            }
        }

        fun isLikelyName(line: String): Boolean {
            val s = line.trim()
            if (s.length < 2 || s.length > 30) return false
            val letters = s.count { it.isLetter() }
            val uppers = s.count { it.isLetter() && it.isUpperCase() }
            return letters > 0 && uppers >= (letters * 0.6)
        }

        if (foundSurname.isNullOrBlank() || foundName.isNullOrBlank()) {
            val bigCaps = lines.filter { isLikelyName(it) }
            if (bigCaps.size >= 2) {
                val sorted = bigCaps.sortedBy { it.length }
                foundSurname = foundSurname ?: sorted.firstOrNull { it.split(' ').size == 1 }
                foundName = foundName ?: bigCaps.firstOrNull { it != foundSurname }
            } else if (bigCaps.size == 1) {
                if (foundSurname.isNullOrBlank()) foundSurname = bigCaps.first()
                else if (foundName.isNullOrBlank()) foundName = bigCaps.first()
            }
        }

        fun clean(s: String?): String? {
            if (s.isNullOrBlank()) return null
            return s.replace(Regex("[^A-Za-zÇĞİIÖŞÜçğıöşü\\s-]"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        val name = clean(foundName)
        val surname = clean(foundSurname)

        return when {
            !name.isNullOrBlank() && !surname.isNullOrBlank() -> "$name $surname"
            !name.isNullOrBlank() -> name
            !surname.isNullOrBlank() -> surname
            else -> null
        }
    }

    // -------- Eşleşme --------
    private fun findMatch(normRead: String, list: List<Participant>): Participant? {
        list.firstOrNull { equalsByName(normRead, normalizeName(it.name ?: "")) }?.let { return it }

        val tokens = normRead.split(' ').filter { it.length >= 2 }
        var best: Participant? = null
        var bestHit = 0
        for (p in list) {
            val pn = normalizeName(p.name ?: "")
            val hit = tokens.count { token -> pn.contains(token) }
            if (hit > bestHit) {
                bestHit = hit
                best = p
            }
        }
        val needed = max(1, tokens.size / 2 + 1)
        return if (best != null && bestHit >= needed) best else null
    }

    private fun normalizeName(s: String): String {
        val replaced = s.map {
            when (it) {
                'ç','Ç' -> 'c'; 'ğ','Ğ' -> 'g'; 'ı','I','İ' -> 'i'; 'ö','Ö' -> 'o'
                'ş','Ş' -> 's'; 'ü','Ü' -> 'u'
                else -> it
            }
        }.joinToString("")
        val noMarks = Normalizer.normalize(replaced, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        return noMarks.lowercase().replace(Regex("\\s+"), " ").trim()
    }

    private fun equalsByName(a: String, b: String): Boolean {
        if (a == b) return true
        val aa = a.split(' ')
        val bb = b.split(' ')
        return (aa.size == 2 && bb.size == 2 && aa[0] == bb[1] && aa[1] == bb[0])
    }

    // -------- UI Helpers --------
    private fun safeUi(block: () -> Unit) {
        if (!isAdded || _binding == null) return
        if (Looper.myLooper() == Looper.getMainLooper()) block()
        else Handler(Looper.getMainLooper()).post { if (isAdded && _binding != null) block() }
    }

    private fun enablePinchToZoom(camera: Camera, previewView: PreviewView) {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val current = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                val delta = detector.scaleFactor
                camera.cameraControl.setZoomRatio((current * delta).coerceIn(1f, 5f))
                return true
            }
        }
        val detector = ScaleGestureDetector(requireContext(), listener)
        previewView.setOnTouchListener { _, event ->
            detector.onTouchEvent(event)
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQ_CAMERA && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startFlow()
        } else {
            Toast.makeText(requireContext(), "Kamera izni gerekli.", Toast.LENGTH_SHORT).show()
        }
    }
}
