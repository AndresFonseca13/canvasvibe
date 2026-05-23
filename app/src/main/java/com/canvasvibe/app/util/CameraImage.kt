package com.canvasvibe.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraImage {

    fun createTempImageUri(context: Context): Uri {
        val cacheImagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File.createTempFile("CV_$timeStamp", ".jpg", cacheImagesDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
