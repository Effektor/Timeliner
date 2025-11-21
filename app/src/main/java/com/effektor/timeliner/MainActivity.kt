package com.effektor.timeliner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.effektor.timeliner.network.Uploader
import com.effektor.timeliner.repository.FolderRepository
import com.effektor.timeliner.repository.TimelineFileRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    
    private lateinit var folderRepository: FolderRepository
    private lateinit var fileRepository: TimelineFileRepository
    private lateinit var uploader: Uploader
    
    private lateinit var btnChooseFolder: Button
    private lateinit var btnOpenSettings: Button
    private lateinit var btnUpload: Button
    private lateinit var tvFolderStatus: TextView
    private lateinit var tvFileStatus: TextView
    private lateinit var tvUploadStatus: TextView
    private lateinit var progressBar: ProgressBar
    
    private var currentFolderUri: Uri? = null
    private var latestFile: TimelineFileRepository.TimelineFile? = null
    
    // Activity result launcher for folder selection
    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            handleFolderSelected(it)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize repositories
        folderRepository = FolderRepository(this)
        fileRepository = TimelineFileRepository(this)
        uploader = Uploader(this)
        
        // Initialize views
        btnChooseFolder = findViewById(R.id.btnChooseFolder)
        btnOpenSettings = findViewById(R.id.btnOpenSettings)
        btnUpload = findViewById(R.id.btnUpload)
        tvFolderStatus = findViewById(R.id.tvFolderStatus)
        tvFileStatus = findViewById(R.id.tvFileStatus)
        tvUploadStatus = findViewById(R.id.tvUploadStatus)
        progressBar = findViewById(R.id.progressBar)
        
        // Set up click listeners
        btnChooseFolder.setOnClickListener {
            launchFolderPicker()
        }
        
        btnOpenSettings.setOnClickListener {
            openLocationSettings()
        }
        
        btnUpload.setOnClickListener {
            uploadLatestFile()
        }
        
        // Load saved folder URI and update UI
        loadSavedFolder()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh file list when returning from settings
        refreshFileStatus()
    }
    
    private fun launchFolderPicker() {
        try {
            folderPickerLauncher.launch(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching folder picker", e)
            Toast.makeText(this, "Error opening folder picker", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun handleFolderSelected(uri: Uri) {
        try {
            // Take persistable permission
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
            
            // Save to repository
            folderRepository.saveFolderUri(uri)
            currentFolderUri = uri
            
            // Update UI
            updateFolderStatus()
            refreshFileStatus()
            
            Toast.makeText(this, "Folder selected successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling folder selection", e)
            Toast.makeText(this, "Error selecting folder: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun loadSavedFolder() {
        currentFolderUri = folderRepository.getFolderUri()
        updateFolderStatus()
        refreshFileStatus()
        updateUploadStatus()
    }
    
    private fun updateFolderStatus() {
        if (currentFolderUri != null) {
            val folderName = currentFolderUri?.lastPathSegment ?: "Selected folder"
            tvFolderStatus.text = getString(R.string.folder_selected, folderName)
        } else {
            tvFolderStatus.text = getString(R.string.folder_not_selected)
        }
    }
    
    private fun refreshFileStatus() {
        val uri = currentFolderUri
        if (uri == null) {
            tvFileStatus.text = getString(R.string.no_folder_selected)
            latestFile = null
            btnUpload.isEnabled = false
            return
        }
        
        try {
            latestFile = fileRepository.findLatestJsonFile(uri)
            
            if (latestFile != null) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                val modifiedDate = Date(latestFile!!.lastModified)
                tvFileStatus.text = getString(
                    R.string.latest_file_found,
                    latestFile!!.name,
                    dateFormat.format(modifiedDate)
                )
                btnUpload.isEnabled = true
            } else {
                tvFileStatus.text = getString(R.string.no_json_files_found)
                btnUpload.isEnabled = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning folder", e)
            tvFileStatus.text = getString(R.string.error_scanning_folder)
            latestFile = null
            btnUpload.isEnabled = false
        }
    }
    
    private fun updateUploadStatus() {
        val lastUploadTime = folderRepository.getLastUploadTime()
        if (lastUploadTime > 0) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val uploadDate = Date(lastUploadTime)
            tvUploadStatus.text = getString(
                R.string.last_upload_success,
                dateFormat.format(uploadDate)
            )
        } else {
            tvUploadStatus.text = getString(R.string.no_uploads_yet)
        }
        
        // Also show the endpoint
        val endpoint = Uploader.getEndpoint()
        tvUploadStatus.append("\n\nEndpoint: $endpoint")
    }
    
    private fun openLocationSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening location settings", e)
            Toast.makeText(this, "Unable to open location settings", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun uploadLatestFile() {
        val file = latestFile
        if (file == null) {
            Toast.makeText(this, "No file to upload", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show progress and disable button
        progressBar.visibility = View.VISIBLE
        btnUpload.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val result = uploader.uploadFile(file.documentFile)
                
                when (result) {
                    is Uploader.UploadResult.Success -> {
                        // Save upload time
                        folderRepository.saveLastUploadTime(System.currentTimeMillis())
                        
                        // Update UI
                        updateUploadStatus()
                        Toast.makeText(
                            this@MainActivity,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is Uploader.UploadResult.Failure -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Upload failed: ${result.error}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during upload", e)
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Hide progress and re-enable button
                progressBar.visibility = View.GONE
                btnUpload.isEnabled = true
            }
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}
