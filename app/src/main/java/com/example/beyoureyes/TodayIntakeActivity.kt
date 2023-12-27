package com.example.beyoureyes

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class TodayIntakeActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button

    private val calorieList : ArrayList<Int> = arrayListOf()

    // total 값도 map의 key로 찾는게 훨씬 편할 것 같아서 map으로 변경
    // 그리고 몇몇 영양수치는 소수점 단위로 떨어져서...(mg 단위) double로 변경
    // 또는 db에 저장하는 값 자체를 전부 mg으로 바꾸면 int로 해도 될 듯
    // 현재는 정수형태는 Long으로(아마 int최대값을 넘어갈까봐 firebase 정수값이 그렇게 설정된 듯), 소수점형태는 Double로 판단되고 있음
    private var totalNutris = mutableMapOf<String, Double>(
        "carbs" to 0.0,
        "cholesterol" to 0.0,
        "fat" to 0.0,
        "natrium" to 0.0,
        "protein" to 0.0,
        "saturatedFat" to 0.0,
        "sugar" to 0.0,
        "transFat" to 0.0
    )
    private var totalCalorie : Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_intake)

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





        Log.d("TODAYINTAKE", "START")
        Toast.makeText(this@TodayIntakeActivity, "START", Toast.LENGTH_SHORT).show()

        val userIdClass = application as userId
        val userId = userIdClass.userId
        val db = Firebase.firestore

        val totalCalorieTextView = findViewById<TextView>(R.id.totalCalorieTextView)

        val naPer = findViewById<TextView>(R.id.naPer)
        val carPer = findViewById<TextView>(R.id.carPer)
        val proPer = findViewById<TextView>(R.id.proPer)
        val choPer = findViewById<TextView>(R.id.choPer)
        val fatPer = findViewById<TextView>(R.id.fatPer)
        val satFatPer = findViewById<TextView>(R.id.satFatPer)
        val sugerPer = findViewById<TextView>(R.id.sugerPer)

        db.collection("userIntakeNutrition")
            .whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("TODAYINTAKE", "${document.id} => ${document.data}")
                    val nutritionMap = document.data["nutrition"] as? Map<String, Any?>
                    val calories = document.data.get("calories") as Long
                    if (nutritionMap != null) {
                        Log.d("TODAYINTAKE", nutritionMap.toString())
                        nutritionMap.forEach { key, value ->

                            // 이 아이템의 key가 영양수치 map에 있는지 확인. 없을 때는 continue.
                            if(!totalNutris.containsKey(key)) return@forEach

                            // value의 타입 체크 후 double로 변경하여 더해주기
                            if ( value is Long ){
                                Log.d("TODAYINTAKE", key + " is Long " + value.toString())
                                totalNutris[key] = totalNutris.getOrDefault(key, 0.0) + value.toDouble()
                            }
                            else if (value is Double) {
                                Log.d("TODAYINTAKE", key + " is Double " + value.toString())
                                totalNutris[key] = totalNutris.getOrDefault(key, 0.0) + value
                            }else if (value is Int) {
                                Log.d("TODAYINTAKE", key + " is Int " + value.toString())
                                totalNutris[key] = totalNutris.getOrDefault(key, 0.0) + value.toDouble()
                            }else{
                                Log.d("TODAYINTAKE", key + " is Any")
                                return@forEach
                            }

                        }
                    }
                    // Firestore에서 가져온 질환 정보 입력
                    if (calories != null) {
                        //calorieList.add(calories.toInt())
                        totalCalorie += calories.toInt()
                    }
                }
                totalCalorieTextView.setText("${totalCalorie.toString()}kcal")
                satFatPer.setText("${totalNutris["saturatedFat"]} g")
                fatPer.setText("${totalNutris["fat"]} g")
                carPer.setText("${totalNutris["carbs"]} g")
                proPer.setText("${totalNutris["protein"]} g")
                choPer.setText("${totalNutris["cholesterol"]?.times(1000)} mg")
                sugerPer.setText("${totalNutris["sugar"]} g")
                naPer.setText("${totalNutris["natrium"]?.times(1000)} mg")
            }
            .addOnFailureListener { exception ->
                Log.w("TODAYINTAKE", "Error getting documents.", exception)
            }

        // 버튼 눌렀을 때 TTS 실행 -> 영양성분 순서 다시 확인 필요 및 % 맞는지 확인 !
        speakButton.setOnClickListener {
            val textToSpeak = "오늘 <날짜> 섭취한 영양소를 분석해드리겠습니다. 오늘 하루 총 섭취한 칼로리는 ${totalCalorie}kcal 입니다. " +
                    "<오늘의 필요 에너지 량을 충족했어요> 세부적인 영양분석은 다음과 같습니다. 나트륨은 ${totalNutris["natrium"]?.times(1000)} %, 탄수화물은  ${totalNutris["carbs"]} % " +
                    "당류는 ${totalNutris["sugar"]} %, 지방은 ${totalNutris["fat"]} %, 포화지방은 ${totalNutris["saturatedFat"]} %, 콜레스테롤은  ${totalNutris["cholesterol"]?.times(1000)} %, 단백질은${totalNutris["carbs"]} % 입니다. "
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