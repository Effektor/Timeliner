package com.effektor.timeliner.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

/**
 * Repository for storing and retrieving the selected folder URI for Timeline exports.
 */
class FolderRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    /**
     * Saves the folder URI string to persistent storage.
     */
    fun saveFolderUri(uri: Uri) {
        prefs.edit()
            .putString(KEY_FOLDER_URI, uri.toString())
            .apply()
    }
    
    /**
     * Retrieves the stored folder URI string, or null if none is saved.
     */
    fun getFolderUri(): Uri? {
        val uriString = prefs.getString(KEY_FOLDER_URI, null) ?: return null
        return try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Clears the stored folder URI.
     */
    fun clearFolderUri() {
        prefs.edit()
            .remove(KEY_FOLDER_URI)
            .apply()
    }
    
    /**
     * Saves the timestamp of the last successful upload.
     */
    fun saveLastUploadTime(timeMillis: Long) {
        prefs.edit()
            .putLong(KEY_LAST_UPLOAD_TIME, timeMillis)
            .apply()
    }
    
    /**
     * Retrieves the timestamp of the last successful upload, or 0 if none.
     */
    fun getLastUploadTime(): Long {
        return prefs.getLong(KEY_LAST_UPLOAD_TIME, 0L)
    }
    
    companion object {
        private const val PREFS_NAME = "timeliner_prefs"
        private const val KEY_FOLDER_URI = "folder_uri"
        private const val KEY_LAST_UPLOAD_TIME = "last_upload_time"
    }
}
