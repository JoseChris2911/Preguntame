package net.azarquiel.firebase2020prueba.tools

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import net.azarquiel.firebase2020prueba.view.MainActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class PickerImage(var activity: Activity ) {
    private lateinit var imagePath: String
    var bitmap:Bitmap? = null
    init {
        checkPermiss()
    }

    // Camera permiss
    private fun checkPermiss() {
        if (    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    MainActivity.REQUEST_PERMISSION
            )
        }
    }

    fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(activity)
        pictureDialog.setTitle("Elige una acción")
        val pictureDialogItems = arrayOf("Galeria", "Camara")
        pictureDialog.setItems(pictureDialogItems
        )
        //{dialog, option}
        { _, option ->
            when (option) {
                0 -> photoFromGallery()
                1 -> photoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun photoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, MainActivity.REQUEST_GALLERY)
    }

    private fun photoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            var photoFile: File? = null
            photoFile = createImageFile()
            photoFile?.let {
                val photoUri = FileProvider.getUriForFile(activity, "${activity.packageName}.provider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                activity.startActivityForResult(intent, MainActivity.REQUEST_CAMERA)
            }
        }

    }


    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MainActivity.REQUEST_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Hasta luego lucas...", Toast.LENGTH_SHORT).show()
                activity.finish()
            }
        }
    }

    @Suppress("DEPRECATION")
    fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                MainActivity.REQUEST_GALLERY -> {
                    data?.let {
                        val contentURI = data.data
                        var filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                        var cursor = activity.contentResolver.query(contentURI!!,
                        filePathColumn, null, null, null)
                        cursor!!.moveToFirst()

                        val columnIndex = cursor!!.getColumnIndex(filePathColumn[0])
                        imagePath = cursor.getString(columnIndex)
                        cursor.close()

                        var bitmap = rotateIfRequired()
                        saveImage(bitmap)
                        this.bitmap = bitmap
                    }
                }
                // data=null para la camara con EXTRA_OUTPUT
                // solo hay data sin EXTRA_OUTPUT (fotos en miniatura)
                MainActivity.REQUEST_CAMERA -> {
                    this.bitmap = rotateIfRequired()
                }
            }
        }
        else{
            Log.d("paco","cancel")
        }

    }


    private fun saveImage(bitmap: Bitmap) {
        var file = createImageFile()

        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        file?.let {
            try {
                val fos = FileOutputStream(file)
                fos.write(bytes.toByteArray())
                MediaScannerConnection.scanFile(activity,
                        arrayOf(file.getPath()),
                        arrayOf("image/jpeg"), null)
                fos.close()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createImageFile(): File? {
        val directory = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!directory!!.exists()) directory.mkdirs()
        try {
            var file = File(directory, ((Calendar.getInstance().getTimeInMillis()).toString() + ".jpg"))
            file.createNewFile()
            imagePath = file.absolutePath
            return file
        }catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun rotateIfRequired():Bitmap{
        var bm = BitmapFactory.decodeFile(imagePath)
        val ei = ExifInterface(imagePath)
        when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                Log.d("paco", "rotada 90")
                return bm rotate 90
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                Log.d("paco", "rotada 180")
                return bm rotate 180
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                Log.d("paco", "rotada 270")
                return bm rotate 270
            }
        }
        return bm
    }

    infix fun Bitmap.rotate(degree: Int): Bitmap {
        val w = width
        val h = height

        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())

        return Bitmap.createBitmap(this, 0, 0, w, h, matrix, true)
    }
}