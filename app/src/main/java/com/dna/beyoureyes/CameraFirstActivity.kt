package com.dna.beyoureyes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dna.beyoureyes.databinding.ActivityCameraFirstBinding
import org.opencv.android.OpenCVLoader

class CameraFirstActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityCameraFirstBinding
    private val camera = Camera()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "영양 정보 촬영하기"

        binding.include.toolbarBackBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }


        // 카메라
        binding.buttoncamera.setOnClickListener {
            while(camera.start(this) == -1){
                camera.start(this)
            }
        }

        //openCV
        OpenCVLoader.initDebug()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {
                Camera.FLAG_REQ_CAMERA -> {
                    camera.processPhoto(this)
                }
            }
        }
    }
}