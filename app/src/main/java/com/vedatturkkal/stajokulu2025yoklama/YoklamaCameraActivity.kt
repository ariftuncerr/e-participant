package com.vedatturkkal.stajokulu2025yoklama

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean
import androidx.core.content.edit

class YoklamaCameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var progressBar: ProgressBar
    private lateinit var stajyerStatus: TextView

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val isProcessing = AtomicBoolean(false)
    private var processingStartTime = 0L

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Kamera izni gerekli", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_yoklama_camera)

        initializeViews()
        setupWindowInsets()
        checkCameraPermission()
    }

    private fun initializeViews() {
        previewView = findViewById(R.id.previewView)
        progressBar = findViewById(R.id.progressBar)
        stajyerStatus = findViewById(R.id.stajyerStatus)

        // Hide status initially
        stajyerStatus.visibility = TextView.GONE
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider)
                updateStatus("Kimlik kartınızı kameraya gösterin")
            } catch (e: Exception) {
                Toast.makeText(this, "Kamera başlatılamadı: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            processImageProxy(imageProxy)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, analyzer)
        } catch (e: Exception) {
            Toast.makeText(this, "Kamera bağlanamadı: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        // Prevent multiple simultaneous processing
        if (isProcessing.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                handleTextRecognitionResult(visionText.text)
            }
            .addOnFailureListener { e ->
                // Log error but continue processing
                updateStatus("Metin tanıma hatası, tekrar deneyin...")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun handleTextRecognitionResult(text: String) {
        if (text.isBlank()) return

        val nameDetails = extractNameAndDetailsFromText(text)
        if (nameDetails != null && nameDetails.isNotBlank()) {
            if (isProcessing.compareAndSet(false, true)) {
                processingStartTime = System.currentTimeMillis()
                showProcessing(true)
                updateStatus("İşleniyor...")

                saveAttendance(nameDetails)
            }
        }
    }

    private fun extractNameAndDetailsFromText(text: String): String? {
        val lines = text.lines()
        var surname: String? = null
        var name: String? = null

        lines.forEachIndexed { i, line ->
            val cleanLine = line.trim()

            // Check for surname indicators
            if (cleanLine.contains("Soyadı", ignoreCase = true) ||
                cleanLine.contains("Surname", ignoreCase = true) ||
                cleanLine.contains("SOYADI", ignoreCase = true)) {

                // Try next line first
                surname = lines.getOrNull(i + 1)?.trim()?.takeIf { it.isNotBlank() }
                // If next line is empty, try the line after "Soyadı:" in same line
                if (surname.isNullOrBlank()) {
                    val parts = cleanLine.split(":", limit = 2)
                    if (parts.size > 1) {
                        surname = parts[1].trim().takeIf { it.isNotBlank() }
                    }
                }
            }

            // Check for name indicators
            else if (cleanLine.contains("Adı", ignoreCase = true) ||
                cleanLine.contains("Given Name", ignoreCase = true) ||
                cleanLine.contains("ADI", ignoreCase = true)) {

                // Try next line first
                name = lines.getOrNull(i + 1)?.trim()?.takeIf { it.isNotBlank() }
                // If next line is empty, try the line after "Adı:" in same line
                if (name.isNullOrBlank()) {
                    val parts = cleanLine.split(":", limit = 2)
                    if (parts.size > 1) {
                        name = parts[1].trim().takeIf { it.isNotBlank() }
                    }
                }
            }
        }

        // Clean up the extracted names
        surname = surname?.replace(Regex("[^a-zA-ZğüşıöçĞÜŞİÖÇ\\s]"), "")?.trim()
        name = name?.replace(Regex("[^a-zA-ZğüşıöçĞÜŞİÖÇ\\s]"), "")?.trim()

        // Validate names - check for obvious OCR errors
        if (!surname.isNullOrBlank() && !name.isNullOrBlank()) {
            val fullName = "$name $surname"

            // Basic validation - reject names with obvious OCR errors
            if (isValidName(name) && isValidName(surname)) {
                return fullName
            }
        }

        return null
    }

    // Validate if a name looks reasonable (basic OCR error detection)
    private fun isValidName(name: String): Boolean {
        // Check minimum length
        if (name.length < 2) return false

        // Check for suspicious character combinations that indicate OCR errors
        val suspiciousPatterns = listOf(
            Regex("[0-9]"), // Numbers in names
            Regex("[^a-zA-ZğüşıöçĞÜŞİÖÇ\\s]"), // Special characters except Turkish chars
            Regex(".*[aeiouıAEIOUI]{4,}.*"), // Too many consecutive vowels
            Regex(".*[bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ]{5,}.*"), // Too many consecutive consonants
            Regex(".*[XQW].*", RegexOption.IGNORE_CASE), // Uncommon letters in Turkish names
            Regex(".*[0O]{2,}.*"), // Multiple O's or 0's together (OCR confusion)
            Regex(".*[Il1]{3,}.*") // Multiple I's, l's or 1's together (OCR confusion)
        )

        return !suspiciousPatterns.any { it.matches(name) }
    }

    private fun saveAttendance(data: String) {
        try {
            val sharedPref = getSharedPreferences("YoklamaPrefs", MODE_PRIVATE)
            val tarih = LocalDate.now().toString()
            val oldData = sharedPref.getString("yoklama_$tarih", "") ?: ""

            // Mevcut kayıtları kontrol et - aynı kişi var mı?
            if (oldData.isNotEmpty()) {
                val existingEntries = oldData.split("\n")
                    .filter { it.isNotBlank() }

                val normalizedNewName = normalizeText(data.trim())
                val newSurname = extractSurname(data.trim())

                for (entry in existingEntries) {
                    val parts = entry.split("|")
                    val existingName = parts[0].trim()
                    val normalizedExistingName = normalizeText(existingName)
                    val existingSurname = extractSurname(existingName)

                    // Tam isim eşleşmesi kontrolü
                    if (normalizedExistingName == normalizedNewName) {
                        showDuplicateWarning(data)
                        return
                    }

                    // Soyadı eşleşmesi kontrolü - daha esnek
                    if (newSurname.isNotEmpty() && existingSurname.isNotEmpty() &&
                        normalizeText(newSurname) == normalizeText(existingSurname)) {

                        // Soyadı aynı ama isim farklıysa onay iste
                        if (normalizedNewName != normalizedExistingName) {
                            // Bu durumda kullanıcıya sorabiliriz veya daha dikkatli kontrol yapabiliriz
                            val similarity = calculateNameSimilarity(normalizedNewName, normalizedExistingName)
                            if (similarity > 0.6) { // %60 benzerlik eşiği
                                showDuplicateWarning("$data (${existingName} ile benzer)")
                                return
                            }
                        }
                    }
                }
            }

            // Eğer kişi daha önce eklenmemişse, normal kayıt işlemini yap
            sharedPref.edit {
                // Zaman bilgisini ekle (format: "AD SOYAD|09:30")
                val currentTime = java.time.LocalTime.now()
                val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                val timeString = currentTime.format(formatter)
                val dataWithTime = "$data|$timeString"

                val newEntry = if (oldData.isEmpty()) dataWithTime else "$oldData\n$dataWithTime"

                putString("yoklama_$tarih", newEntry)
            }

            // Başarılı kayıt mesajı ve ana sayfaya dönüş
            runOnUiThread {
                Toast.makeText(this, "$data yoklaması başarıyla alındı!", Toast.LENGTH_LONG).show()

                // Return to main activity with result
                val intent = Intent(this, Home::class.java).apply {
                    putExtra("attendance_result", data)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                finish()
            }

        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Kayıt hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                // Hata durumunda da işlemi sıfırla
                isProcessing.set(false)
                showProcessing(false)
                updateStatus("Kimlik kartınızı kameraya gösterin")
            }
        }
    }

    private fun showDuplicateWarning(name: String) {
        runOnUiThread {
            Toast.makeText(
                this,
                "$name zaten bugün yoklama listesinde kayıtlı!",
                Toast.LENGTH_LONG
            ).show()

            // İşlemi sıfırla ve kameraya geri dön
            isProcessing.set(false)
            showProcessing(false)
            updateStatus("Kimlik kartınızı kameraya gösterin")
        }
    }

    private fun extractSurname(fullName: String): String {
        val parts = fullName.trim().split(" ")
        return if (parts.size > 1) parts.last() else ""
    }

    private fun calculateNameSimilarity(name1: String, name2: String): Double {
        val shorter = if (name1.length < name2.length) name1 else name2
        val longer = if (name1.length > name2.length) name1 else name2

        val editDistance = levenshteinDistance(shorter, longer)
        return (longer.length - editDistance) / longer.length.toDouble()
    }

    private fun levenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }

        for (i in 0..str1.length) {
            for (j in 0..str2.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> {
                        val cost = if (str1[i-1] == str2[j-1]) 0 else 1
                        dp[i][j] = minOf(
                            dp[i-1][j] + 1,      // deletion
                            dp[i][j-1] + 1,      // insertion
                            dp[i-1][j-1] + cost  // substitution
                        )
                    }
                }
            }
        }

        return dp[str1.length][str2.length]
    }

    // Türkçe karakterleri normalize eden yardımcı fonksiyon
    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace('ç', 'c')
            .replace('ğ', 'g')
            .replace('ı', 'i')
            .replace('ö', 'o')
            .replace('ş', 's')
            .replace('ü', 'u')
            .replace('â', 'a')
            .replace('î', 'i')
            .replace('û', 'u')
            .trim()
    }

    private fun showProcessing(show: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (show) ProgressBar.VISIBLE else ProgressBar.GONE
        }
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            stajyerStatus.text = message
            stajyerStatus.visibility = TextView.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer.close()
    }
}