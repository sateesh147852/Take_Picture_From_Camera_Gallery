package com.takeImage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.takeImage.databinding.ActivityMainBinding
import com.takeImage.utils.Constants.FOLDER_PATH
import com.takeImage.utils.Constants.NAME
import com.takeImage.utils.Utility
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


const val CAMERA_PERMISSION_CODE = 100

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private var bottomSheetDialog: BottomSheetDialog? = null
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoFile: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
        setOnClickListener()
    }

    private fun initialize() {
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { savePhotoInGallery(it) }
                } else {
                    Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show()
                }
            }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    Toast.makeText(this, "Image is taken from camera", Toast.LENGTH_SHORT).show()
                    val myBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    binding.ivImage.setImageBitmap(myBitmap)
                } else {
                    Toast.makeText(this, "Image is not taken from camera", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun savePhotoInGallery(data: Uri) {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".png"
        val storageFile =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/" + FOLDER_PATH + NAME + timeStamp)
        val inputStream = contentResolver.openInputStream(data)
        val outputStream = FileOutputStream(storageFile)
        Utility.copyStream(inputStream, outputStream)
        binding.ivImage.setImageBitmap(BitmapFactory.decodeFile(storageFile.absolutePath))

    }

    private fun setOnClickListener() {
        binding.btTakePhoto.setOnClickListener(this)
    }

    override fun onClick(view: View) {

        when (view.id) {

            binding.btTakePhoto.id -> takePhoto()

            R.id.tvCamera -> {
                Utility.createDirectory(this)
                bottomSheetDialog?.dismiss()
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoFile = createImageFile()

                val photoUri = if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
                    FileProvider.getUriForFile(this, "com.takeImage.fileProvider", photoFile)
                } else {
                    Uri.fromFile(photoFile)
                }



                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                cameraLauncher.launch(takePictureIntent)
            }

            R.id.tvGallery -> {
                Utility.createDirectory(this)
                bottomSheetDialog?.dismiss()
                val pictureActionIntent = Intent(Intent.ACTION_GET_CONTENT)
                pictureActionIntent.type = "image/*"
                galleryLauncher.launch(pictureActionIntent)
            }

            R.id.tvCancel -> {
                bottomSheetDialog?.dismiss()
            }
        }
    }

    private fun takePhoto() {
        if (checkPermissions()) {
            showCameraDialog()
        } else {
            requestCameraPermissions()
        }
    }

    private fun showCameraDialog() {
        if (bottomSheetDialog == null)
            bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog?.let {
            it.setContentView(R.layout.picture_layout)
            it.show()
            val tvCamera = it.findViewById<MaterialTextView>(R.id.tvCamera)
            val tvGallery = it.findViewById<MaterialTextView>(R.id.tvGallery)
            val tvCancel = it.findViewById<MaterialTextView>(R.id.tvCancel)
            tvCamera?.setOnClickListener(this)
            tvGallery?.setOnClickListener(this)
            tvCancel?.setOnClickListener(this)
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".jpg"
        return File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/" + FOLDER_PATH + NAME + timeStamp)
    }

    private fun requestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun checkPermissions(): Boolean {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        )
            return true
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            takePhoto()
        else
            requestCameraPermissions()

    }

}