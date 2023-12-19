package com.example.beyoureyes

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix

import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat

import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class CameraFirstActivity : AppCompatActivity()  {

    // 권한 처리에 필요한 변수
    val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)

    val FLAG_PERM_CAMERA = 98

    val FLAG_REQ_CAMERA = 101
    val BLUR_SCORE_THRESH = 40

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_first)

        // 카메라
        val camera = findViewById<Button>(R.id.buttoncamera)
        camera.setOnClickListener {
            if(isPermission(CAMERA_PERMISSION)){
                dispatchTakePictureIntent()
            } else {
                ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, FLAG_PERM_CAMERA)
            }
        }

        //openCV
        OpenCVLoader.initDebug()

    }

    fun isPermission(permissions:Array<String>) : Boolean {

        for(permission in permissions) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.beyoureyes.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, FLAG_REQ_CAMERA)
                }
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {
                FLAG_REQ_CAMERA -> {

                    val f = File(currentPhotoPath)
                    val uri = Uri.fromFile(f)

                    var bitmap: Bitmap? = null

                    if(Build.VERSION.SDK_INT < 28) {
                        bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            uri
                        )
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, uri)
                        bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.setTargetSampleSize(1) // shrinking by
                            decoder.isMutableRequired = true // this resolve the hardware type of bitmap problem
                        }
                    }



                    if (bitmap != null) {

                        // rotation check
                        val ei = ExifInterface(currentPhotoPath)
                        val orientation: Int = ei.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED
                        )

                        bitmap = when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> bitmap
                            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, -90f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 180f)
                            ExifInterface.ORIENTATION_NORMAL -> rotateImage(bitmap, 90f)
                            else -> bitmap
                        }

                        // blur check
                        val blur_score = getBlurScore(bitmap)
                        if ( blur_score < BLUR_SCORE_THRESH) {
                            // 재촬영 요구 - intent 연결
                            intent = Intent(this, CameraFocusProblemActivity::class.java)

                        }else{

                            val intent = Intent(this, LoadingActivity::class.java)
                            val output = histogramEqual(bitmap)

                            // 파일로 비트맵 저장
                            val stream = FileOutputStream(f)
                            output.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            stream.close()

                            // 파일 경로를 인텐트에 추가
                            intent.putExtra("bitmapPath", f.absolutePath)
                            startActivity(intent)
                        }
                    }else{
                        Toast.makeText(this, "사진 촬영에 실패하였습니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun getBlurScore(bitmap: Bitmap): Double {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val src = Mat()
        Utils.bitmapToMat(outputBitmap, src)
        // to grayscale
        val graySrc = Mat()
        Imgproc.cvtColor(src, graySrc, Imgproc.COLOR_BGR2GRAY)
        val laplacian = Mat()
        Imgproc.Laplacian(graySrc, laplacian, CvType.CV_64F)

        // Calculate the variance of the Laplacian
        val variance = Mat()
        Core.multiply(laplacian, laplacian, variance)
        val mean = Core.mean(variance)
        return mean.`val`[0]
    }

    fun histogramEqual(bitmap: Bitmap): Bitmap {
        // val output = Mat()
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val image = Mat()
        Utils.bitmapToMat(outputBitmap, image)

        // Color image
        if (image.channels() == 4) {
            val ycrcbArray = Mat()
            Imgproc.cvtColor(image, ycrcbArray, Imgproc.COLOR_RGB2YCrCb)

            val channels = ArrayList<Mat>(3)
            Core.split(ycrcbArray, channels)

            Imgproc.equalizeHist(channels[0], channels[0])

            Core.merge(channels, ycrcbArray)
            Imgproc.cvtColor(ycrcbArray, image, Imgproc.COLOR_YCrCb2RGB)
        }
        // Gray image
        else {
            Imgproc.equalizeHist(image, image)
        }

        Utils.matToBitmap(image, outputBitmap)

        return outputBitmap
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]); }
        }
    }

}