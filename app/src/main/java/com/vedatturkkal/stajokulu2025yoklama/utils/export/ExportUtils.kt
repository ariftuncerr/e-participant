package com.vedatturkkal.stajokulu2025yoklama.utils.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ExportUtils {

    /**
     * FileProvider authority: ${applicationId}.fileprovider
     * AndroidManifest ve xml/file_paths ayarlı olmalı.
     */
    fun shareFile(context: Context, file: File, mime: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, title))
    }
}
