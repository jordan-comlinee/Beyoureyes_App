package com.example.beyoureyes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import java.util.Locale
import android.app.Activity
import android.content.Intent
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.chip.ChipGroup

class FoodInfoAllergyPersonalizedActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private val camera = Camera()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_allergy_personalized)

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

        // 버튼 눌렀을 때 TTS 실행 -> 수정 예정
        speakButton.setOnClickListener {
            val textToSpeak =
                "당신의 맞춤별 영양 정보를 분석해드리겠습니다. 해당 식품에는 ${allergyList?.joinToString(", ")}가 함유되어 있습니다. 해당 식품의 모든 정보를 확인하고" +
                        "싶으시면 모든 정보 확인하기 버튼을 클릭해주세요. 또한 해당 섭취 시 먹기 버튼을 클릭하고 먹은 양의 정보를 알려주세요."
            speak(textToSpeak)

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
        }

        // intent로 전달받은 식품 정보 파싱
        val allergyList = intent.getStringArrayListExtra("allergyList")

        if (allergyList.isNullOrEmpty())
            Log.d("test", "list is null")
        else
            Log.d("test", allergyList.joinToString())

        // 알러지 표시 ------------------------------------------------------
        val allergyChipGroup: ChipGroup = findViewById<ChipGroup>(R.id.allergyChipGroup1)
        val allergyTextView = findViewById<TextView>(R.id.allergyMsg)

        val allergyChipView = AllergyChipView(allergyChipGroup, allergyTextView)

        AppUser.info?.allergic?.let { userAllergy -> // 사용자 알러지 정보 꺼내기
            allergyList?.let { foodAllergy ->        // 식품 알러지 정보 꺼내기
                allergyChipView.set(this, foodAllergy.toTypedArray(), userAllergy.toTypedArray())
            }
        }


        val retryButton = findViewById<Button>(R.id.buttonRetry)

        retryButton.setOnClickListener {
            while (camera.start(this) == -1) {
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