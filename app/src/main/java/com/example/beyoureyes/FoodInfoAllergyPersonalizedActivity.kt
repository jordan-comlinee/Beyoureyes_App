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
import com.example.beyoureyes.databinding.ActivityFoodInfoAllergyPersonalizedBinding
import com.google.android.material.chip.ChipGroup

class FoodInfoAllergyPersonalizedActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private val camera = Camera()
    private lateinit var binding: ActivityFoodInfoAllergyPersonalizedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodInfoAllergyPersonalizedBinding.inflate(layoutInflater)  // Initialize the binding
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


        // intent로 전달받은 식품 정보 파싱
        val allergyList = intent.getStringArrayListExtra("allergyList")

        if (allergyList.isNullOrEmpty())
            Log.d("test", "list is null")
        else
            Log.d("test", allergyList.joinToString())

        // 알러지 표시 ------------------------------------------------------
        val allergyChipGroup: ChipGroup = binding.allergyChipGroup
        val allergyTextView = binding.allergyMsg

        val allergyChipView = AllergyChipView(allergyChipGroup, allergyTextView)

        AppUser.info?.allergic?.let { userAllergy -> // 사용자 알러지 정보 꺼내기
            allergyList?.let { foodAllergy ->        // 식품 알러지 정보 꺼내기
                allergyChipView.set(this, foodAllergy.toTypedArray(), userAllergy.toTypedArray())
            }
        }


        binding.buttonRetry.setOnClickListener {
            while (camera.start(this) == -1) {
                camera.start(this)
            }
        }

        // 모든 정보 표시 버튼
        binding.buttonGeneralize.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.none, R.anim.none)
        }

        // 버튼 초기화
        speakButton = binding.buttonVoice

        // 버튼 눌렀을 때 TTS 실행 -> 수정 예정
        speakButton.setOnClickListener {
            AppUser.info?.allergic?.let { userAllergy -> // 사용자 알러지 정보 꺼내기
                allergyList?.let { foodAllergy ->        // 식품 알러지 정보 꺼내기
                    val commonAllergens = userAllergy.intersect(foodAllergy)
                    if (commonAllergens.isNotEmpty()) {
                        val allergyMsg = "해당 식품에는 당신이 유의해야 할 ${commonAllergens.joinToString()}이 함유되어 있습니다."
                        speak(allergyMsg)
                    } else {
                        speak("해당 식품에는 당신의 알러지 성분이 함유되어 있지 않습니다.")
                    }
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