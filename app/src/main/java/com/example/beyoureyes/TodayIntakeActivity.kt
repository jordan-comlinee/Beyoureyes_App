package com.example.beyoureyes

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
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.PieChart
import com.google.firebase.Timestamp

import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

import org.threeten.bp.format.DateTimeFormatter

class TodayIntakeActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_intake)
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)    // 화면 전환 시 애니메이션

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
        val userIdClass = application as userId
        val userId = userIdClass.userId
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

        // 영양성분별 섭취량 표시
        val naPer = findViewById<TextView>(R.id.naPer)
        val carPer = findViewById<TextView>(R.id.carPer)
        val proPer = findViewById<TextView>(R.id.proPer)
        val choPer = findViewById<TextView>(R.id.choPer)
        val fatPer = findViewById<TextView>(R.id.fatPer)
        val satFatPer = findViewById<TextView>(R.id.satFatPer)
        val sugerPer = findViewById<TextView>(R.id.sugerPer)

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
            .whereEqualTo("userID", userId)
            .whereGreaterThanOrEqualTo("date", startOfToday) // 오늘 날짜 해당하는 것만
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) { // 쿼리 결과 없을 때(오늘 섭취량 기록 아직 없음)
                    energyChart.hide() // 차트 숨김
                    energyReviewText.showNoDataMsg(this)

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
                    speakButton = findViewById(R.id.buttonlisten)

                    // 버튼 눌렀을 때 TTS 실행 -> 영양성분 순서 다시 확인 필요 및 % 맞는지 확인 !
                    speakButton.setOnClickListener {
                        val textToSpeak = "오늘 <날짜> 섭취한 영양소를 분석해드리겠습니다. " +
                                "오늘 하루 총 섭취한 칼로리는 ${totalIntake.energy}kcal 입니다. " +
                                "<오늘의 필요 에너지 량을 충족했어요> 세부적인 영양분석은 다음과 같습니다. " +
                                "나트륨은 ${totalIntake.natrium?.getMilliGram()}mg, " +
                                "탄수화물은  ${totalIntake.carbs?.getGram()}g, " +
                                "당류는 ${totalIntake.sugar?.getGram()}g, " +
                                "지방은 ${totalIntake.fat?.getGram()}g, " +
                                "포화지방은 ${totalIntake.satFat?.getGram()}g, " +
                                "콜레스테롤은  ${totalIntake.chol?.getMilliGram()}mg, " +
                                "단백질은${totalIntake.protein?.getGram()}g 입니다. "
                        speak(textToSpeak)
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
        // TTS 해제
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()

        super.onDestroy()

    }
}