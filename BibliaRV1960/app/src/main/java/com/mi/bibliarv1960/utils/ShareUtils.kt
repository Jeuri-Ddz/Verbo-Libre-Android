package com.mi.bibliarv1960.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    /**
     * Guarda un Bitmap en la caché y lanza el selector de compartir de Android.
     */
    fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        captionText: String,
    ) {
        val uri = saveBitmapToCache(context, bitmap)
        if (uri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, captionText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir desde Verbo Libre"))
        }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        val imagesFolder = File(context.cacheDir, "shared_images")
        try {
            imagesFolder.mkdirs()
            // Limpiar imágenes anteriores para no saturar la caché
            imagesFolder.listFiles()?.forEach { it.delete() }
            
            val file = File(imagesFolder, "share_verse_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            
            return FileProvider.getUriForFile(
                context, 
                "${context.packageName}.fileprovider", 
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
