package com.dna.beyoureyes

import TTSManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import java.util.Locale
import android.app.Activity
import android.content.Intent
import com.dna.beyoureyes.databinding.ActivityFoodInfoAllergyPersonalizedBinding
import com.google.android.material.chip.ChipGroup

class FoodInfoAllergyPersonalizedActivity : AppCompatActivity() {

    private lateinit var ttsManager: TTSManager
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
        // TTSManager 초기화 완료되었을때
        ttsManager = TTSManager(this) {
            // 버튼 눌렀을 때 TTS 실행
            speakButton.setOnClickListener {
                AppUser.info?.allergic?.let { userAllergy -> // 사용자 알러지 정보 꺼내기
                    allergyList?.let { foodAllergy ->        // 식품 알러지 정보 꺼내기
                        val commonAllergens = userAllergy.intersect(foodAllergy)
                        if (commonAllergens.isNotEmpty()) {
                            val allergyMsg =
                                "당신의 맞춤별 영양 정보를 분석해드리겠습니다. 해당 식품에는 당신이 유의해야 할 ${commonAllergens.joinToString()}이 함유되어 있습니다. 영양 성분 정보는 인식되지 않았습니다. 추가적인 정보를 원하시면 화면에 다시찍기 버튼을 눌러주세요"

                            ttsManager.speak(allergyMsg)
                        } else {
                            ttsManager.speak(
                                "당신의 맞춤별 영양 정보를 분석해드리겠습니다. 해당 식품에는 당신의 알러지 성분이 함유되어 있지 않습니다." +
                                        " 영양 성분 정보는 인식되지 않았습니다. 추가적인 정보를 원하시면 화면에 다시찍기 버튼을 눌러주세요."
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        ttsManager.shutdown()
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