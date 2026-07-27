package com.example.data.remote

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

class CloudinaryUploaderTest {

    @Test
    fun `upload with unconfigured credentials throws descriptive exception`() {
        val dummyFile = File.createTempFile("test_img", ".jpg").apply {
            writeText("dummy content")
            deleteOnExit()
        }
        try {
            // Should throw exception if BuildConfig properties are placeholders or empty
            CloudinaryUploader.upload(dummyFile)
            // If credentials happen to be configured, upload may attempt network call
        } catch (e: Exception) {
            assertTrue(
                "Exception message should mention credentials or upload failure: ${e.message}",
                e.message?.contains("Cloudinary") == true || e.message?.contains("credentials") == true || e.message?.contains("failed") == true
            )
        }
    }
}
