package com.dna.beyoureyes

import TTSManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale
import com.dna.beyoureyes.databinding.ActivityTodayIntakeBinding
import com.google.firebase.Timestamp

import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

import org.threeten.bp.format.DateTimeFormatter

class TodayIntakeActivity : AppCompatActivity() {

    private lateinit var ttsManager: TTSManager
    private lateinit var speakButton: Button
    private lateinit var binding: ActivityTodayIntakeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayIntakeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)    // 화면 전환 시 애니메이션

        speakButton = binding.buttonlisten

        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "오늘의 영양소 확인"

        binding.include.toolbarBackBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
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

        // 나트륨 섭취 비율
        val naPer: TextView = binding.naPer
        // 탄수화물 섭취 비율
        val carPer: TextView = binding.carPer
        // 단백질 섭취 비율
        val proPer: TextView = binding.proPer
        // 콜레스테롤 섭취 비율
        val choPer: TextView = binding.choPer
        // 지방 섭취 비율
        val fatPer: TextView = binding.fatPer
        // 포화 지방 섭취 비율
        val satFatPer: TextView = binding.satFatPer
        // 당류 섭취 비율
        val sugerPer: TextView = binding.sugerPer

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
        Log.d("TODAYINTAKE", "${current} => ${startOfToday.toDate()}")

        db.collection("userIntakeNutrition")
            .whereEqualTo("userID", AppUser.id)
            .whereGreaterThanOrEqualTo("date", startOfToday) // 오늘 날짜 해당하는 것만
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) { // 쿼리 결과 없을 때(오늘 섭취량 기록 아직 없음)
                    energyChart.hide() // 차트 숨김
                    energyReviewText.showNoDataMsg(this)
                    // TTSManager 초기화 완료되었을때
                    ttsManager = TTSManager(this) {
                        speakButton.setOnClickListener {
                            val textToSpeech = "오늘의 섭취량 기록이 없습니다. 분석 결과를 제공받기 위해서 기록을 남겨보세요."
                            ttsManager.speak(textToSpeech)
                        }
                    }

                } else { // 쿼리 결과 있을 때
                    // 2.1. 총 섭취량 구하기
                    var totalIntake = NutritionFacts()

                    for (document in result) {
                        // 섭취량 합계 연산
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
                    val energyDV = NutrientDailyValues().energy
                    energyReviewText.setTextViews(this, energyIntake, energyDV)

                    // 2.4. 총 섭취량 화면 표시 - 성분별 섭취량
                    totalIntake.natrium?.let {nat -> naPer.setText("${nat.getMilliGram()}mg")}
                    totalIntake.carbs?.let {carbs -> carPer.setText("${carbs.getGram()}g")}
                    totalIntake.sugar?.let {sug -> sugerPer.setText("${sug.getGram()}g")}
                    totalIntake.protein?.let {prot -> proPer.setText("${prot.getGram()}g")}
                    totalIntake.fat?.let {fat -> fatPer.setText("${fat.getGram()}g")}
                    totalIntake.satFat?.let {sf -> satFatPer.setText("${sf.getGram()}g")}
                    totalIntake.chol?.let {chol -> choPer.setText("${chol.getMilliGram()}mg")}

                    //////////////////////////////////////////////////////////
                    // TTSManager 초기화 완료되었을때
                    ttsManager = TTSManager(this) {
                        // 버튼 눌렀을 때 TTS 실행
                        speakButton.setOnClickListener {
                            val textToSpeak =
                                "${dateText.text}의 섭취량 기록을 분석해드리겠습니다.${totalCalorieTextView.text}" +
                                        "나트륨은 ${totalIntake.natrium?.getMilliGram()}mg, " +
                                        "탄수화물은  ${totalIntake.carbs?.getGram()}g, " +
                                        "당류는 ${totalIntake.sugar?.getGram()}g, " +
                                        "지방은 ${totalIntake.fat?.getGram()}g, " +
                                        "포화지방은 ${totalIntake.satFat?.getGram()}g, " +
                                        "콜레스테롤은  ${totalIntake.chol?.getMilliGram()}mg, " +
                                        "단백질은${totalIntake.protein?.getGram()}g 입니다. "
                            ttsManager.speak(textToSpeak)
                        }
                    }

                }

            }
            .addOnFailureListener { exception ->
                Log.w("TODAYINTAKE", "Error getting documents.", exception)

                // DB 연결 에러 처리
                energyChart.hide()
                energyReviewText.showErrorMsg()
            }

    }
    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()

    }
}