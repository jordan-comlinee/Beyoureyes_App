package com.example.beyoureyes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import java.util.Locale
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.PieChart
import com.google.android.material.chip.ChipGroup

class FoodInfoNutritionPersonalizedActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private val camera = Camera()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_nutrition_personalized)

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
        val totalKcal = intent.getIntExtra("totalKcal", 0)

        val nutriFactsInMilli = intent.getIntegerArrayListExtra("nutriFactsInMilliString")
        nutriFactsInMilli?.let {
            Log.d("test", it.joinToString())
        }

        // 영양성분 정보 객체 생성
        val nutriFacts = NutritionFacts(nutriFactsInMilli!!.toIntArray(), totalKcal)
        Log.d("test", nutriFacts.toString())

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
                findViewById<TextView>(R.id.line7_label), findViewById<TextView>(R.id.line7_percent))
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

        // 모든 정보 표시 버튼
        val btnGeneral = findViewById<Button>(R.id.buttonGeneralize)

        btnGeneral.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.none, R.anim.none)
        }

        val retryButton = findViewById<Button>(R.id.buttonRetry)

        retryButton.setOnClickListener {
            while(camera.start(this) == -1){
                camera.start(this)
            }
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

        val koreanCharacterList = listOf("나트륨", "탄수화물", "당류", "지방", "포화지방", "콜레스테롤", "단백질")

        // 버튼 초기화
        // speakButton = findViewById(R.id.)

        // 버튼 눌렀을 때 TTS 실행 -> 수정 예정 -> 여기서 칼로리는 제공하지 않아도되지 않을까???-> 회의 시간에 애기!!
        speakButton.setOnClickListener {
            val calorieText = "칼로리는 <~kcal> 입니다."
            val nutrientsText = buildString {
                for (i in koreanCharacterList.indices) {
                    append("${koreanCharacterList[i]}은 <퍼센트 리스트>")
                    if (i < koreanCharacterList.size - 1) {
                        append(", ")
                    }
                }
            }

            val textToSpeak = "당신의 맞춤별 영양 정보를 분석해드리겠습니다. 해당식품의 $calorieText 또한 영양 성분 정보는 일일 권장량 당 $nutrientsText 입니다. 해당 식품의 모든 정보를 확인하고" +
                    "싶으시면 모든 정보 확인하기 버튼을 클릭해주세요. 또한 해당 식품 섭취 시 먹기 버튼을 클릭하고 먹은 양의 정보를 알려주세요."
            speak(textToSpeak)
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