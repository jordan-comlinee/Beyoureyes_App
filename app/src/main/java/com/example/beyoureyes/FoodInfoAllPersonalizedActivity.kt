package com.example.beyoureyes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.setPadding
import com.github.mikephil.charting.charts.PieChart
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class FoodInfoAllPersonalizedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_all_personalized)

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
        Log.d("test", user.disease.toString() + ", " + user.allergic.toString())

        // intent로 전달받은 식품 정보 파싱
        val totalKcal = intent.getIntExtra("totalKcal", 0)

        val nutriFactsInMilli = intent.getIntegerArrayListExtra("nutriFactsInMilliString")
        nutriFactsInMilli?.let {
            Log.d("test", it.joinToString())
        }
        val allergyList = intent.getStringArrayListExtra("allergyList")
        // 영양성분 정보 객체 생성
        val nutriFacts = NutritionFacts(nutriFactsInMilli!!.toIntArray(), totalKcal)

        // 에너지 섭취 비율 원형 차트
        val chart = findViewById<PieChart>(R.id.pieChartScanSuccess)
        val energyChart = EnergyChart(chart)
        nutriFacts.carbs?.let { carbs ->
            nutriFacts.protein?.let { protein ->
                nutriFacts.fat?.let { fat -> // 탄단지 객체 null safe 처리

                    // 탄단지 에너지값 설정
                    energyChart.setCaloreisFromMilliGram(
                        carbs.getMilliGram(),
                        protein.getMilliGram(),
                        fat.getMilliGram()
                    )

                    // 차트 표시 설정
                    energyChart.setChart(this)
                }
            }
        }

        // 칼로리 표시
        val calorieTextView = findViewById<TextView>(R.id.kcalValue)
        calorieTextView.text = "${totalKcal}kcal"


        // 영양성분 표시 ----------------------------------------
        val cautionTextView = findViewById<TextView>(R.id.nutri_caution)
        val line0 = findViewById<TextView>(R.id.line0)

        val lineViewsList = arrayListOf<PercentOfDailyValueLineView>(
            PercentOfDailyValueLineView(
                findViewById<TextView>(R.id.line1_label), findViewById<TextView>(R.id.line1_percent)),
            PercentOfDailyValueLineView(
                findViewById<TextView>(R.id.line2_label), findViewById<TextView>(R.id.line2_percent)),
            PercentOfDailyValueLineView(
                findViewById<TextView>(R.id.line3_label), findViewById<TextView>(R.id.line3_percent)),
            PercentOfDailyValueLineView(
                findViewById<TextView>(R.id.line4_label), findViewById<TextView>(R.id.line4_percent)),
            PercentOfDailyValueLineView(
                findViewById<TextView>(R.id.line5_label), findViewById<TextView>(R.id.line5_percent)),
            PercentOfDailyValueLineView(
                findViewById<TextView>(R.id.line6_label), findViewById<TextView>(R.id.line6_percent)),
            PercentOfDailyValueLineView(
                findViewById<TextView>(R.id.line7_label), findViewById<TextView>(R.id.line7_percent)),
        )

        val percentView = PercentViewOfNutritionFacts(cautionTextView, line0, lineViewsList)


        val userDVs = user.getDailyValues()
        if (user.hasDisease()) { // 질환 있을 시
            percentView.setWarningText(user.disease!!) // 경고 문구 설정
            percentView.setLineViews(this, nutriFacts, userDVs, user.getNutrisToCare()) // 퍼센트 라인 설정
        }else { // 질환 없을 시
            percentView.hideWarningText() // 경고 문구 없애기
            percentView.setLineViews(nutriFacts, userDVs) // 퍼센트 라인 설정
        }

        // 알러지 표시 ------------------------------------------------------
        val allergyChipGroup: ChipGroup = findViewById<ChipGroup>(R.id.allergyChipGroup1)
        val allergyTextView = findViewById<TextView>(R.id.allergyMsg)
        val allergyChipView = AllergyChipView(allergyChipGroup, allergyTextView)

        user.allergic?.let { userAllergy ->
            allergyList?.let { foodAllergy ->
                allergyChipView.set(this, foodAllergy.toTypedArray(), userAllergy)
            }
        }

        // 모든 정보 표시 버튼
        val btnGeneral = findViewById<Button>(R.id.buttonGeneralize)

        btnGeneral.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.none, R.anim.none)
        }

    }
}