import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.vedatturkkal.stajokulu2025yoklama.data.repository.ParticipantRepository
import com.vedatturkkal.stajokulu2025yoklama.databinding.FragmentExcelImportActivityBinding
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ExcelImportParticipantDialogFragment(private val activityId: String) : DialogFragment() {

    private lateinit var binding: FragmentExcelImportActivityBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        binding = FragmentExcelImportActivityBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)

        // Dosya seç butonuna tıklama
        binding.importExcelButton.setOnClickListener {
            openFilePicker()
        }

        return builder.create()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        startActivityForResult(Intent.createChooser(intent, "Excel Dosyası Seç"), 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            readExcelFromUri(uri)
        }
    }

    private fun readExcelFromUri(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                    ?: throw Exception("Dosya açılamadı")

                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)

                val participantRepository = ParticipantRepository()
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("Kullanıcı oturumu bulunamadı")

                var addedCount = 0

                for (i in sheet.firstRowNum + 1..sheet.lastRowNum) { // Başlık atla
                    val row = sheet.getRow(i) ?: continue
                    val cell = row.getCell(1) // B sütunu
                    val name = cell?.toString()?.trim()

                    if (!name.isNullOrBlank()) {
                        val success = participantRepository.addParticipant(activityId, name)
                        if (success) addedCount++
                    }
                }

                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        if (addedCount > 0) {
                            Snackbar.make(binding.root, "$addedCount katılımcı başarıyla eklendi", Snackbar.LENGTH_LONG).show()
                        } else {
                            Snackbar.make(binding.root, "Hiç katılımcı eklenemedi.", Snackbar.LENGTH_LONG).show()
                        }
                        dismiss()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Snackbar.make(binding.root, "Dosya okunamadı: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

}
