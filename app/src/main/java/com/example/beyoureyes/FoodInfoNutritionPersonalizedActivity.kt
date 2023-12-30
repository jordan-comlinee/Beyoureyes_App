package com.example.beyoureyes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import java.util.Locale

class FoodInfoNutritionPersonalizedActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_nutrition_personalized)

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
        speakButton = findViewById(R.id.buttonVoice)

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

            val textToSpeak = "당신의 맞춤별 영양 정보를 분석해드리겠습니다. 해당식품의 $calorieText 또한 영양 성분 정보는 당신의 일일 권장량 $nutrientsText 입니다. 해당 식품의 모든 정보를 확인하고" +
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
}
