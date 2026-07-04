package com.example.data.remote

import org.json.JSONObject
import java.io.DataOutputStream
import java.io.File
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import com.example.BuildConfig

/**
 * Uploads images to Cloudinary using Android's native HttpsURLConnection.
 * No additional libraries needed — uses the same networking stack as Firebase.
 * Call only from a background thread / IO coroutine.
 */
object CloudinaryUploader {

    private val CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME
    private const val FOLDER     = "products"
    private const val UPLOAD_PRESET = "unsigned_preset"
    private val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    /**
     * Uploads [file] to Cloudinary and returns the public HTTPS URL.
     * Throws an exception if the upload fails — the caller handles it.
     */
    fun upload(file: File): String {
        val boundary = "----RiceMartBoundary${System.currentTimeMillis()}"
        val lineEnd  = "\r\n"
        val twoHyphens = "--"

        val connection = (URL(UPLOAD_URL).openConnection() as HttpsURLConnection).apply {
            requestMethod = "POST"
            doInput  = true
            doOutput = true
            useCaches = false
            connectTimeout = 30_000
            readTimeout    = 60_000
            setRequestProperty("Connection", "Keep-Alive")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        DataOutputStream(connection.outputStream).use { out ->

            // Helper to write a text field
            fun writeField(name: String, value: String) {
                out.writeBytes("$twoHyphens$boundary$lineEnd")
                out.writeBytes("Content-Disposition: form-data; name=\"$name\"$lineEnd$lineEnd")
                out.writeBytes("$value$lineEnd")
            }

            writeField("upload_preset", UPLOAD_PRESET)
            writeField("folder",    FOLDER)

            // Write the image file part
            out.writeBytes("$twoHyphens$boundary$lineEnd")
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"$lineEnd")
            out.writeBytes("Content-Type: image/jpeg$lineEnd$lineEnd")
            file.inputStream().use { it.copyTo(out) }
            out.writeBytes(lineEnd)

            // Closing boundary
            out.writeBytes("$twoHyphens$boundary$twoHyphens$lineEnd")
            out.flush()
        }

        val responseCode = connection.responseCode
        val responseBody = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().readText()
        } else {
            val err = try { connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error" }
                      catch (_: Exception) { "Unknown error" }
            throw Exception("Cloudinary upload failed ($responseCode): $err")
        }
        connection.disconnect()

        return JSONObject(responseBody).getString("secure_url")
    }
}
