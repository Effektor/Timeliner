package com.effektor.timeliner.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Repository for scanning and finding Timeline JSON files in the selected folder.
 */
class TimelineFileRepository(private val context: Context) {
    
    /**
     * Data class representing information about a Timeline JSON file.
     */
    data class TimelineFile(
        val documentFile: DocumentFile,
        val name: String,
        val lastModified: Long
    )
    
    /**
     * Scans the folder at the given URI and returns the most recently modified JSON file.
     * Returns null if:
     * - The URI is invalid
     * - The folder is empty
     * - No JSON files are found
     * - Permission issues occur
     */
    fun findLatestJsonFile(folderUri: Uri): TimelineFile? {
        try {
            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return null
            
            if (!folder.exists() || !folder.isDirectory) {
                return null
            }
            
            val jsonFiles = folder.listFiles()
                .filter { file ->
                    file.isFile && 
                    file.name?.lowercase()?.endsWith(".json") == true &&
                    file.canRead()
                }
            
            if (jsonFiles.isEmpty()) {
                return null
            }
            
            // Find the file with the maximum lastModified timestamp
            val latestFile = jsonFiles.maxByOrNull { it.lastModified() } ?: return null
            
            return TimelineFile(
                documentFile = latestFile,
                name = latestFile.name ?: "unknown.json",
                lastModified = latestFile.lastModified()
            )
        } catch (e: Exception) {
            // Log error but return null to indicate failure
            android.util.Log.e(TAG, "Error finding latest JSON file", e)
            return null
        }
    }
    
    /**
     * Lists all JSON files in the folder, sorted by last modified time (newest first).
     */
    fun listAllJsonFiles(folderUri: Uri): List<TimelineFile> {
        try {
            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return emptyList()
            
            if (!folder.exists() || !folder.isDirectory) {
                return emptyList()
            }
            
            return folder.listFiles()
                .filter { file ->
                    file.isFile && 
                    file.name?.lowercase()?.endsWith(".json") == true &&
                    file.canRead()
                }
                .map { file ->
                    TimelineFile(
                        documentFile = file,
                        name = file.name ?: "unknown.json",
                        lastModified = file.lastModified()
                    )
                }
                .sortedByDescending { it.lastModified }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error listing JSON files", e)
            return emptyList()
        }
    }
    
    companion object {
        private const val TAG = "TimelineFileRepository"
    }
}
