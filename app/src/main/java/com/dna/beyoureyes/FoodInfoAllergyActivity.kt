package com.dna.beyoureyes

import android.annotation.SuppressLint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import com.dna.beyoureyes.databinding.ActivityFoodInfoAllergyBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


import java.util.Locale

class FoodInfoAllergyActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private lateinit var personalButton:Button
    private val camera = Camera()
    private lateinit var binding: ActivityFoodInfoAllergyBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodInfoAllergyBinding.inflate(layoutInflater)  // Initialize the binding
        setContentView(binding.root)

        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "영양 분석 결과"

        binding.include.toolbarBackBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // 먹기 버튼
        val eatbutton = binding.buttoneat

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

        // 알러지 정보 intent하여 표시
        val allergyChipGroup: ChipGroup = binding.allergyChipGroup
        val allergyList = intent.getStringArrayListExtra("allergyList")

        if (allergyList != null) {
            for (diseaseItem in allergyList) {
                val chip = Chip(this)
                chip.text = diseaseItem

                // Chip 뷰의 크기 및 여백 설정
                val params = ChipGroup.LayoutParams(
                    250, // 넓이 80
                    150  // 높이 50
                )
                params.setMargins(8, 8, 8, 8) // 여백을 8로..
                chip.layoutParams = params

                // 글씨 크기
                chip.textSize = 25f

                // 가운데 정렬
                chip.textAlignment = View.TEXT_ALIGNMENT_CENTER
                chip.setPadding(20, 20, 20, 20) // 상, 좌, 하, 우 패딩
                allergyChipGroup.addView(chip)
            }
        }

        // 버튼 눌렀을 때 TTS 실행
        speakButton.setOnClickListener {
            val textToSpeak = "영양 정보를 분석해드리겠습니다. 해당 식품에는 ${allergyList?.joinToString(", ")}가 함유되어 있습니다. 영양 성분 정보는 인식되지 않았습니다. 추가적인 정보를 원하시면 화면에 다시 찍기 버튼을 눌러주세요." +
                    " 또한 해당 식품 섭취 시 먹기 버튼을 클릭하고 먹은 양의 정보를 알려주세요."
            speak(textToSpeak)
        }

        binding.buttonRetry.setOnClickListener {
            while(camera.start(this) == -1){
                camera.start(this)
            }
        }

        // 맞춤 정보 버튼
        personalButton = binding.buttonPersonalized

        // 사용자 맞춤 서비스 제공 여부 검사(맞춤 정보 있는지)
        // 기존 Firebase와의 통신 코드는 다 제거
        AppUser.info?.let { // 사용자 정보 있을 시

            val intent = Intent(this, FoodInfoAllergyPersonalizedActivity::class.java) //OCR 실패시 OCR 가이드라인으로 이동
            // 식품 정보 전달 (알레르기 only)
            intent.putExtra("allergyList", allergyList)
            // 이제 intent로 사용자 정보 전달할 필요 X

            // 맞춤 정보 버튼 활성화
            personalButton.setOnClickListener {
                startActivity(intent)
                overridePendingTransition(R.anim.none, R.anim.none)
            }
        } ?: run {// 사용자 정보 없을 시
            // 맞춤 정보 버튼 비활성화
            personalButton.isEnabled = false // 버튼 비활성화
            personalButton.setBackgroundResource(R.drawable.button_grey) // 비활성화 drawable 추가함
        }

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


    override fun onDestroy() {
        // TTS 해제
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()

        super.onDestroy()

    }

    override fun onBackPressed() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        if(isFinishing()){
            overridePendingTransition(R.anim.none, R.anim.horizon_exit)
        }
    }

}