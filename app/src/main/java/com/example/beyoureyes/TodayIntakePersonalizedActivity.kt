package com.example.beyoureyes

import android.content.Intent
import android.graphics.Color

import android.os.Bundle
import android.view.ViewGroup

import android.graphics.Typeface
import android.speech.tts.TextToSpeech

import android.util.Log
import android.view.View
import android.widget.Button

import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.beyoureyes.databinding.ActivityTodayIntakePersonalizedBinding
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
import java.util.Locale

class TodayIntakePersonalizedActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private lateinit var binding: ActivityTodayIntakePersonalizedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayIntakePersonalizedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)

        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "오늘의 영양소 확인"

        binding.include.toolbarBackBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // TextToSpeech 초기화
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.KOREAN)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language is not supported or missing data")
                } else {
                    // TTS 초기화 성공
                    Log.d("TTS", "TextToSpeech initialization successful")
                }
            } else {
                Log.e("TTS", "TextToSpeech initialization failed")
            }
        }

        fun speak(text: String) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val params = Bundle()
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "UniqueID")
            } else {
                // LOLLIPOP 이하의 버전에서는 UtteranceId를 지원하지 않음
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null)
            }
        }

        // 버튼 초기화
        speakButton = binding.buttonVoice

        fun convertColorIntToRgb(colorInt: Int): Triple<Int, Int, Int> {
            val red = colorInt shr 16 and 0xFF
            val green = colorInt shr 8 and 0xFF
            val blue = colorInt and 0xFF
            return Triple(red, green, blue)
        }

        fun evaluateIntakeStatus(rgb: Triple<Int, Int, Int>): String {
            val (red, green, blue) = rgb

            return when {
                red > 200 && green > 150 && blue < 50 -> "적정량 부족" // (241, 188, 0)
                red > 50 && green > 200 && blue < 50 -> "적정량 안정" // (52, 202, 0)
                red > 200 && green < 50 && blue < 50 -> "적정량 초과" // (255, 0, 0)
                else -> "알 수 없음"
            }
        }


        // Firebase 연결을 위한 설정값
        val db = Firebase.firestore

        // 에너지 섭취 비율 원형 차트
        val chart = binding.pieChart
        val energyChart = EnergyChart(chart)

        // 날짜 표시
        val dateText = binding.date

        // 총 섭취 칼로리 표시
        val totalCalorieTextView = binding.totalCalorieTextView
        val calorieReview1 = binding.carlReview1
        val calorieReview2 = binding.carlReview2
        val energyReviewText = EnergyReview(totalCalorieTextView, calorieReview1, calorieReview2)

        // 영양성분 바 표시
        // 영양성분 바 표시
        val carboBarChart: HorizontalBarChart = binding.carboBarchart
        val carboDVTextView: TextView = binding.carboDV
        val carboIconTextView: TextView = binding.carboIcon

        val sugBarChart: HorizontalBarChart = binding.sugBarchart
        val sugDVTextView: TextView = binding.sugDV
        val sugIconTextView: TextView = binding.sugIcon

        val fatBarChart: HorizontalBarChart = binding.fatBarchart
        val fatDVTextView: TextView = binding.fatDV
        val fatIconTextView: TextView = binding.fatIcon

        val proteinBarChart: HorizontalBarChart = binding.proBarchart
        val proteinDVTextView: TextView = binding.proDV
        val proteinIconTextView: TextView = binding.proIcon

        val naBarChart: HorizontalBarChart = binding.naBarchart
        val naDVTextView: TextView = binding.naDV
        val naIconTextView: TextView = binding.naIcon

        val choleBarChart: HorizontalBarChart = binding.choleBarchart
        val choleDVTextView: TextView = binding.choleDV
        val choleIconTextView: TextView = binding.choleIcon

        val satFatBarChart: BarChart = binding.satFatBarchart
        val satFatDVTextView: TextView = binding.satFatDV
        val satFatIconTextView: TextView = binding.satFatIcon

        // 영양성분 섭취량 총평
        val lackIntakeReviewTextView: TextView = binding.lackIntakeReview
        val overIntakeReviewTextView: TextView = binding.overIntakeReview

        val nat = NutriIntakeBarDisplay(naBarChart,naDVTextView, naIconTextView)
        val carbo = NutriIntakeBarDisplay(carboBarChart,carboDVTextView, carboIconTextView)
        val sugar = NutriIntakeBarDisplay(sugBarChart,sugDVTextView, sugIconTextView)
        val protein = NutriIntakeBarDisplay(proteinBarChart,proteinDVTextView, proteinIconTextView)
        val fat = NutriIntakeBarDisplay(fatBarChart,fatDVTextView, fatIconTextView)
        val satfat = NutriIntakeBarDisplay(satFatBarChart,satFatDVTextView, satFatIconTextView)
        val chole = NutriIntakeBarDisplay(choleBarChart,choleDVTextView, choleIconTextView)

        val intakeBars = AllIntakeBarDisplay(
            nat, carbo, sugar, protein, fat, satfat, chole,
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

                    speakButton.setOnClickListener {
                        val textToSpeech = "오늘의 섭취량 기록이 없습니다. 분석 결과를 제공받기 위해서 기록을 남겨보세요."
                        speak(textToSpeech)
                    }

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

                    // 초과, 적정, 부족 상태 판단
                    val naStatus = evaluateIntakeStatus(convertColorIntToRgb(nat.getBarColor() ?: 0))
                    val carboStatus = evaluateIntakeStatus(convertColorIntToRgb(carbo.getBarColor() ?: 0))
                    val sugarStatus = evaluateIntakeStatus(convertColorIntToRgb(sugar.getBarColor() ?: 0))
                    val proteinStatus = evaluateIntakeStatus(convertColorIntToRgb(protein.getBarColor() ?: 0))
                    val fatStatus = evaluateIntakeStatus(convertColorIntToRgb(fat.getBarColor() ?: 0))
                    val satfatStatus = evaluateIntakeStatus(convertColorIntToRgb(satfat.getBarColor() ?: 0))
                    val choleStatus = evaluateIntakeStatus(convertColorIntToRgb(chole.getBarColor() ?: 0))

                    speakButton.setOnClickListener {
                        val textToSpeech = "${dateText.text}의 섭취량 기록을 분석해드리겠습니다.${totalCalorieTextView.text} 또한 오늘 섭취한 나트륨은 ${naStatus}, 탄수화물은 ${carboStatus}, " +
                                "당류는 ${sugarStatus}, 지방은 ${fatStatus}, 포화지방은 ${satfatStatus}, 콜레스테롤은 ${choleStatus}, 단백질은 ${proteinStatus} 입니다."
                        speak(textToSpeech)
                    }

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

    override fun onDestroy() {
        // TTS 해제
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()

        super.onDestroy()

    }

}