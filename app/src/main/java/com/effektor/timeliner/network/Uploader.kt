package com.effektor.timeliner.network

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.effektor.timeliner.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Handles uploading Timeline JSON files to the configured web service.
 */
class Uploader(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Result of an upload operation.
     */
    sealed class UploadResult {
        data class Success(val message: String) : UploadResult()
        data class Failure(val error: String) : UploadResult()
    }
    
    /**
     * Uploads the given DocumentFile to the configured endpoint.
     * This is a suspend function that should be called from a coroutine.
     * 
     * @param file The DocumentFile to upload
     * @return UploadResult indicating success or failure
     */
    suspend fun uploadFile(file: DocumentFile): UploadResult = withContext(Dispatchers.IO) {
        try {
            // Read the file content
            val inputStream = context.contentResolver.openInputStream(file.uri)
                ?: return@withContext UploadResult.Failure("Unable to open file for reading")
            
            val jsonContent = inputStream.use { stream ->
                stream.bufferedReader().use { it.readText() }
            }
            
            // Validate that it's not empty
            if (jsonContent.isBlank()) {
                return@withContext UploadResult.Failure("File is empty")
            }
            
            // Create the request
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonContent.toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(BuildConfig.TIMELINE_UPLOAD_ENDPOINT)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            // Execute the request
            Log.d(TAG, "Uploading file: ${file.name} to ${BuildConfig.TIMELINE_UPLOAD_ENDPOINT}")
            
            val response = client.newCall(request).execute()
            
            response.use {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "Upload successful: ${response.code} - $responseBody")
                    UploadResult.Success("Upload successful (HTTP ${response.code})")
                } else {
                    val errorBody = response.body?.string() ?: "No error details"
                    Log.e(TAG, "Upload failed: ${response.code} - $errorBody")
                    UploadResult.Failure("Upload failed: HTTP ${response.code} - $errorBody")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during upload", e)
            UploadResult.Failure("Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during upload", e)
            UploadResult.Failure("Error: ${e.message}")
        }
    }
    
    companion object {
        private const val TAG = "Uploader"
        
        /**
         * Returns the configured endpoint URL.
         * Useful for displaying in the UI.
         */
        fun getEndpoint(): String = BuildConfig.TIMELINE_UPLOAD_ENDPOINT
    }
}
