package com.example.beyoureyes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.PieChart
import com.google.android.material.chip.ChipGroup

class FoodInfoAllergyPersonalizedActivity : AppCompatActivity() {

    private val camera = Camera()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_allergy_personalized)

        // toolBar 및 뒤로가기 설정
        val toolBar = findViewById<Toolbar>(R.id.toolbarDefault)
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        val toolbarBackButton = findViewById<ImageButton>(R.id.toolbarBackBtn)
        setSupportActionBar(toolBar)
        // Toolbar에 앱 이름 표시 제거!!
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle.setText("맞춤 영양 분석 결과")
        toolbarBackButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // intent로 전달받은 사용자 파라미터 파싱 + 사용자 객체 생성
        val user = UserInfo(
            "tmp",
            intent.getIntExtra("userAge", 0),
            intent.getIntExtra("userSex", 0),
            intent.getStringArrayExtra("userDisease"),
            intent.getStringArrayExtra("userAllergic")
        )
        Log.d("test", user.disease?.joinToString() + ", " + user.allergic?.joinToString())

        // intent로 전달받은 식품 정보 파싱
        val allergyList = intent.getStringArrayListExtra("allergyList")

        if(allergyList.isNullOrEmpty())
            Log.d("test", "list is null")
        else
            Log.d("test", allergyList.joinToString())

        // 알러지 표시 ------------------------------------------------------
        val allergyChipGroup: ChipGroup = findViewById<ChipGroup>(R.id.allergyChipGroup1)
        val allergyTextView = findViewById<TextView>(R.id.allergyMsg)

        val allergyChipView = AllergyChipView(allergyChipGroup, allergyTextView)

        user.allergic?.let { userAllergy ->
            allergyList?.let { foodAllergy ->
                Log.d("test", foodAllergy.toString())
                allergyChipView.set(this, foodAllergy.toTypedArray(), userAllergy)
            }
        }

        val retryButton = findViewById<Button>(R.id.buttonRetry)

        retryButton.setOnClickListener {
            while(camera.start(this) == -1){
                camera.start(this)
            }
        }

        // 모든 정보 표시 버튼
        val btnGeneral = findViewById<Button>(R.id.buttonGeneralize)

        btnGeneral.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.none, R.anim.none)
        }

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