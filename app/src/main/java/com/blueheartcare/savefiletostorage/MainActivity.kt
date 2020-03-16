package com.blueheartcare.savefiletostorage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider.getUriForFile
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*


class MainActivity : AppCompatActivity() {
    var PICKFILE_REQUEST_CODE = 101
    var ACCESS_REQUEST_CODE = 102
    var TAG = "MainActivityTag"

    private val DEFAULT_PICTURE_NAME = "Example.jpg"
    private val REQUEST_TAKE_PHOTO = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSelectFile.setOnClickListener { v: View? ->
            selectFileCheck()
        }
    }

    fun selectFileCheck() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestForSpecificPermission()
        } else {
            selectFile()
        }
    }

    private fun requestForSpecificPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            ACCESS_REQUEST_CODE
        )
    }

    fun selectFile() {
//
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
//        startActivityForResult(intent, PICKFILE_REQUEST_CODE)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = createImageFile()
            var photoURI:Uri
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                photoURI = Uri.fromFile(photoFile)
                Log.d(TAG," if photoURI $photoURI")
            }
            else {
                photoURI = getUriForFile(this,BuildConfig.APPLICATION_ID, photoFile!!)
                Log.d(TAG," else photoURI $photoURI")

            }
            takePictureIntent.flags =Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)

        }
    }
    private fun createImageFile(): File? {
        val state: String = Environment.getExternalStorageState()
        val filesDir: File
        // Make sure it's available
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            filesDir = File(
                Environment.getExternalStorageDirectory().toString() + "/Example/Media",
                "Example Images"
            )
        } else {
//            // Load another directory, probably local memory
//        filesDir = new File(getFilesDir(),"Images");
            filesDir = File(getExternalFilesDir(null), "Example Images")
        }
        if (!filesDir.exists()) filesDir.mkdirs()
        Log.d(TAG,"filesDir $filesDir")
        return File(filesDir, DEFAULT_PICTURE_NAME)
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {

        when (requestCode) {
            ACCESS_REQUEST_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFile()

            } else {
                selectFileCheck()

            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && intent?.data != null) {

            tvSelectFile.text = intent.dataString

//            createDir()
//            saveFile(intent.data!!.path!!)

        }else if (requestCode==REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK && intent?.data != null){
            tvSelectFile.text = intent.getStringExtra(MediaStore.EXTRA_OUTPUT)
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }

    private fun createDir() {

        var directory = File("/sdcard/BHC/images")
        if (directory.exists()){
        }else{
            directory.mkdirs()
        }
    }

    fun saveFile(myFile: String) {
        var sourceLocation=File(myFile)
        var targetLocation=File("/sdcard/BHC/images")

        Log.d(TAG,"sourceLocation $sourceLocation")
        Log.d(TAG,"targetLocation $targetLocation")

        if (sourceLocation.exists()) {
            val inFile: InputStream = FileInputStream(sourceLocation)
            val outFile: OutputStream = FileOutputStream(targetLocation)

            val buf = ByteArray(1024)
            var len = inFile.read(buf)
            while (len > 0) {
                outFile.write(buf, 0, len)
            }
            inFile.close()
            outFile.close()
            Log.d(TAG, "Copy file successful.")
        } else {
            Log.d(TAG, "Copy file failed. Source file missing.")
        }
    }

}
