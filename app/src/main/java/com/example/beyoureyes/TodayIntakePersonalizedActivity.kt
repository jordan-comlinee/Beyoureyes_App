package com.example.beyoureyes

import android.content.Intent
import android.graphics.Color

import android.os.Bundle
import android.view.ViewGroup

import android.graphics.Typeface

import android.util.Log
import android.view.View

import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.charts.PieChart
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import com.jakewharton.threetenabp.AndroidThreeTen

class TodayIntakePersonalizedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_intake_personalized)
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)

        // toolBar 및 뒤로가기 설정
        val toolBar = findViewById<Toolbar>(R.id.toolbarDefault)
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        val toolbarBackButton = findViewById<ImageButton>(R.id.toolbarBackBtn)
        setSupportActionBar(toolBar)

        // Toolbar에 앱 이름 표시 제거!!
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle.setText("오늘의 영양소 확인")
        toolbarBackButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            //overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_enter)
        }

        // Firebase 연결을 위한 설정값
        val db = Firebase.firestore

        // 에너지 섭취 비율 원형 차트
        val chart = findViewById<PieChart>(R.id.pieChart)
        val energyChart = EnergyChart(chart)

        // 날짜 표시
        val dateText = findViewById<TextView>(R.id.date)

        // 총 섭취 칼로리 표시
        val totalCalorieTextView = findViewById<TextView>(R.id.totalCalorieTextView)
        val calorieReview1 = findViewById<TextView>(R.id.carlReview1)
        val calorieReview2 = findViewById<TextView>(R.id.carlReview2)
        val energyReviewText = EnergyReview(totalCalorieTextView, calorieReview1, calorieReview2)

        // 영양성분 바 표시
        val carboBarChart = findViewById<HorizontalBarChart>(R.id.carboBarchart)
        val carboDVTextView = findViewById<TextView>(R.id.carboDV)
        val carboIconTextView = findViewById<TextView>(R.id.carboIcon)

        val sugBarChart = findViewById<HorizontalBarChart>(R.id.sugBarchart)
        val sugDVTextView = findViewById<TextView>(R.id.sugDV)
        val sugIconTextView = findViewById<TextView>(R.id.sugIcon)

        val fatBarChart = findViewById<HorizontalBarChart>(R.id.fatBarchart)
        val fatDVTextView = findViewById<TextView>(R.id.fatDV)
        val fatIconTextView = findViewById<TextView>(R.id.fatIcon)

        val proteinBarChart = findViewById<HorizontalBarChart>(R.id.proBarchart)
        val proteinDVTextView = findViewById<TextView>(R.id.proDV)
        val proteinIconTextView = findViewById<TextView>(R.id.proIcon)

        val naBarChart = findViewById<HorizontalBarChart>(R.id.naBarchart)
        val naDVTextView = findViewById<TextView>(R.id.naDV)
        val naIconTextView = findViewById<TextView>(R.id.naIcon)

        val choleBarChart = findViewById<HorizontalBarChart>(R.id.choleBarchart)
        val choleDVTextView = findViewById<TextView>(R.id.choleDV)
        val choleIconTextView = findViewById<TextView>(R.id.choleIcon)

        val satFatBarChart = findViewById<BarChart>(R.id.satFatBarchart)
        val satFatDVTextView = findViewById<TextView>(R.id.satFatDV)
        val satFatIconTextView = findViewById<TextView>(R.id.satFatIcon)

        // 영양성분 섭취량 총평
        val lackIntakeReviewTextView = findViewById<TextView>(R.id.lackIntakeReview)
        val overIntakeReviewTextView = findViewById<TextView>(R.id.overIntakeReview)

        val intakeBars = AllIntakeBarDisplay(
            NutriIntakeBarDisplay(naBarChart,naDVTextView, naIconTextView),
            NutriIntakeBarDisplay(carboBarChart,carboDVTextView, carboIconTextView),
            NutriIntakeBarDisplay(sugBarChart,sugDVTextView, sugIconTextView),
            NutriIntakeBarDisplay(proteinBarChart,proteinDVTextView, proteinIconTextView),
            NutriIntakeBarDisplay(fatBarChart,fatDVTextView, fatIconTextView),
            NutriIntakeBarDisplay(satFatBarChart,satFatDVTextView, satFatIconTextView),
            NutriIntakeBarDisplay(choleBarChart,choleDVTextView, choleIconTextView),
            lackIntakeReviewTextView,
            overIntakeReviewTextView
        )

        // 1. 오늘 날짜 표시
        // 현재 api 레벨 최소 설정이 24라 호환 문제(LocalDateTime 사용에 26이상 필요)
        // -> java.time 대신 threeten으로 백포팅 적용
        AndroidThreeTen.init(this)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        dateText.text = current.format(formatter)

        // 2. Firebase DB로부터 사용자 데이터 쿼리
        // 오늘 날짜(의 시작)를 firebase timestamp 형식으로 변경(쿼리를 위해)
        val today = current.toLocalDate().atStartOfDay()
        val startOfToday = Timestamp(today.toEpochSecond(ZoneOffset.UTC), today.nano)

        // 사용자 맞춤 권장량 구하기
        val userDVs = AppUser.info?.getDailyValues()

        // DB에서 총 섭취량 가져오기
        db.collection("userIntakeNutrition")
            .whereEqualTo("userID", AppUser.id)
            .whereGreaterThanOrEqualTo("date", startOfToday) // 오늘 날짜 해당하는 것만
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) { // 쿼리 결과 없을 때(오늘 섭취량 기록 아직 없음)
                    energyChart.hide() // 차트 숨김
                    energyReviewText.showNoDataMsg(this)
                    intakeBars.hide(this, userDVs)

                } else { // 쿼리 결과 있을 때
                    // 2.1. 총 섭취량 구하기
                    var totalIntake = NutritionFacts()

                    // 섭취량 합계 연산
                    for (document in result) {
                        Log.d("TODAYINTAKE", "${document.id} => ${document.data}")
                        val nutritionMap = document.data["nutrition"] as? Map<String, Any?>
                        val calories = document.data.get("calories") as Long

                        var intake = NutritionFacts()

                        if (calories != null) {
                            intake.setEnergyValue(calories.toInt())
                        }
                        if (nutritionMap != null) {
                            intake.setNutritionValues(nutritionMap)
                        }
                        totalIntake += intake
                    }

                    // 2.2. 총 섭취량 화면 표시 - 에너지 섭취 비율 차트
                    totalIntake.carbs?.let { carbs ->
                        totalIntake.protein?.let { protein ->
                            totalIntake.fat?.let { fat -> // 탄단지 객체 null safe 처리

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

                    // 2.3. 총 섭취량 화면 표시 - 총 칼로리 평가
                    val energyIntake = totalIntake.energy ?: 0
                    energyReviewText.setTextViews(this, energyIntake, userDVs?.energy)

                    // 2.4. 총 섭취량 화면 표시 - 성분별 섭취량 바
                    intakeBars.setAll(this, totalIntake, userDVs)
                }

            }
            .addOnFailureListener { exception ->
                Log.w("TODAYINTAKE", "Error getting documents.", exception)

                // DB 연결 에러 처리
                energyChart.hide()
                energyReviewText.showErrorMsg()
                intakeBars.hide(this, userDVs)

            }

    }

}