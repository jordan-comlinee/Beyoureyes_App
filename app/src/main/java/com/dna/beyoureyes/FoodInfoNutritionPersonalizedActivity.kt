package com.dna.beyoureyes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import java.util.Locale
import com.dna.beyoureyes.databinding.ActivityFoodInfoNutritionPersonalizedBinding

class FoodInfoNutritionPersonalizedActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private val camera = Camera()
    private lateinit var binding: ActivityFoodInfoNutritionPersonalizedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodInfoNutritionPersonalizedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "맞춤 영양 분석 결과"

        binding.include.toolbarBackBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // 먹기 버튼
        val eatButton = binding.buttoneat

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
        val chart = binding.pieChart
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
        val calorieTextView = binding.kcaltextview
        calorieTextView.text = "${totalKcal}kcal"

        // 영양성분 표시 ----------------------------------------
        val cautionTextView = binding.nutricaution
        val line0 = binding.line0

        val lineViewsList = arrayListOf<PercentOfDailyValueLineView>(
            PercentOfDailyValueLineView(
                binding.line1Label, binding.line1Percent),
            PercentOfDailyValueLineView(
                binding.line2Label, binding.line2Percent),
            PercentOfDailyValueLineView(
                binding.line3Label, binding.line3Percent),
            PercentOfDailyValueLineView(
                binding.line4Label, binding.line4Percent),
            PercentOfDailyValueLineView(
                binding.line5Label, binding.line5Percent),
            PercentOfDailyValueLineView(
                binding.line6Label, binding.line6Percent),
            PercentOfDailyValueLineView(
                binding.line7Label, binding.line7Percent)
        )

        val percentView = PercentViewOfNutritionFacts(cautionTextView, line0, lineViewsList)

        // 사용자 맞춤 권장량 계산
        val userDVs = AppUser.info?.getDailyValues()

        // 권장량 대비 영양소 함유 퍼센트 표시 설정
        AppUser.info?.disease?.let { disease -> // 사용자가 질환 있을 시
            percentView.setWarningText(disease) // 경고 문구 설정
            percentView.setLineViews(this,
                nutriFacts, userDVs, AppUser.info!!.getNutrisToCare())
        }?:run{ // 질환 없을 시
            percentView.hideWarningText() // 경고 문구 없애기
            percentView.setLineViews(nutriFacts, userDVs)
        }

        // 모든 정보 표시 버튼
        val btnGeneral = binding.buttonGeneralize

        btnGeneral.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.none, R.anim.none)
        }

        binding.buttonRetry.setOnClickListener {
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


        // 버튼 초기화
        speakButton = binding.buttonVoice

        // 버튼 눌렀을 때 TTS 실행 -> 수정 예정 -> 여기서 칼로리는 제공하지 않아도되지 않을까???-> 회의 시간에 애기!!
        speakButton.setOnClickListener {
            val calorieText = "칼로리는 ${totalKcal}kcal 입니다."
            val nutrientsText = buildString {
                for (i in lineViewsList.indices) {
                    val nutrientName = lineViewsList[i].labelTextView.text.toString().removePrefix("ㄴ")
                    val nutrientPercent = lineViewsList[i].percentTextView.text.toString()
                    append("$nutrientName 은 $nutrientPercent")

                    if (i < lineViewsList.size - 1) {
                        append(", ")
                    }
                }
            }

            val textToSpeak = "당신의 맞춤별 영양 정보를 분석해드리겠습니다. 해당식품의 $calorieText 또한 영양 성분 정보는 당신의 일일 권장량 당 $nutrientsText 입니다." +
                    " 알레르기 정보는 인식되지 않았습니다. 추가적인 정보를 원하시면 화면에 다시 찍기 버튼을 눌러주세요. " +
                    "또한 해당 식품 섭취 시 먹기 버튼을 클릭하고 먹은 양의 정보를 알려주세요."
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