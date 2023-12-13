package com.example.beyoureyes

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Locale

class FoodInfoAllergyActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_allergy)

        //toolBar
        val toolBar = findViewById<Toolbar>(R.id.toolbarDefault)
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        val toolbarBackButton = findViewById<ImageButton>(R.id.toolbarBackBtn)
        setSupportActionBar(toolBar)
        //Toolbar에 앱 이름 표시 제거!!
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle.setText("영양 분석 결과")

        toolbarBackButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            //overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_enter)
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
        speakButton = findViewById(R.id.buttonVoice)


        // 알러지 정보 intent하여 표시
        val allergyChipGroup: ChipGroup = findViewById<ChipGroup>(R.id.allergyChipGroup1)
        val allergyList = intent.getStringArrayListExtra("allergyList")

        if (allergyList != null) {
            for (diseaseItem in allergyList) {
                val chip = Chip(this)
                chip.text = diseaseItem

                // Chip 뷰의 크기 및 여백 설정
                val params = ChipGroup.LayoutParams(
                    200, // 넓이 80
                    150  // 높이 50
                )
                params.setMargins(8, 8, 8, 8) // 여백을 8로..
                chip.layoutParams = params

                // 글씨 크기
                chip.textSize = 25f

                // 가운데 정렬
                chip.textAlignment = View.TEXT_ALIGNMENT_CENTER

                allergyChipGroup.addView(chip)
            }
        }

        // 버튼 눌렀을 때 TTS 실행 -> 수정 예정
        speakButton.setOnClickListener {
            val textToSpeak = "안녕하세요! 영양 정보를 분석해드리겠습니다. 해당 식품에는 ${allergyList?.joinToString(", ")}가 함유되어 있습니다. 다른 영양 성분 정보는 인식되지 않았습니다. 추가적인 정보를 원하시면 화면에 다시찍기 버튼을 눌러주세요."
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