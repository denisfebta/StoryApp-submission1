package com.denis.storyapp.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.denis.storyapp.R
import com.denis.storyapp.custom.CustomDescription
import com.denis.storyapp.data.Resource
import com.denis.storyapp.databinding.ActivityAddStoryBinding
import com.denis.storyapp.utils.UserPreferences
import com.denis.storyapp.utils.createTempFile
import com.denis.storyapp.utils.reduceFileImage
import com.denis.storyapp.utils.uriToFile
import com.denis.storyapp.viewmodel.AddViewModel
import com.denis.storyapp.viewmodel.ViewModelFactory
import com.uk.tastytoasty.TastyToasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var addViewModel: AddViewModel
    private lateinit var pref: UserPreferences
    private lateinit var currentPhotoPath: String
    private var getFile: File? = null

    companion object {

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Story"

        binding.btnAddCamera.setOnClickListener { startTakePhoto() }
        binding.btnAddGallery.setOnClickListener { startGallery() }

        binding.btnUpload.setOnClickListener {
            addStory()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        addStory()
        setupViewModel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Do not get permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun setupViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        addViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[AddViewModel::class.java]

        addViewModel.uploadStory.observe(this@AddStoryActivity) {
            when (it) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(true)
                    Toast.makeText(this@AddStoryActivity, it.data, Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
                is Resource.Error -> {
                    showLoading(true)
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun addStory() {
        binding.etDescription.globalChange()
        binding.btnUpload.setOnClickListener {
            if (getFile != null) {
                val file = reduceFileImage(getFile as File)
                val descriptionText = binding.etDescription.text.toString()
                val description = descriptionText.toRequestBody("text/plain".toMediaType())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                CoroutineScope(Dispatchers.IO).launch {
                    addViewModel.uploadImage(imageMultipart, description)
                }
            } else {
                TastyToasty.error(this@AddStoryActivity, "Please insert the picture").show()
            }
          setupViewModel()
        }
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "com.denis.storyapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile

            val result = BitmapFactory.decodeFile(getFile?.path)
            binding.ivPreview.setImageBitmap(result)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri

            val myFile = uriToFile(selectedImg, this@AddStoryActivity)

            getFile = myFile

            binding.ivPreview.setImageURI(selectedImg)
        }
    }

    private fun CustomDescription.globalChange() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                with(binding) {
                    btnUpload.isEnabled = etDescription.isValid == true && getFile != null
                }
            }
        })
    }
}