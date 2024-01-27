package com.dna.beyoureyes

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dna.beyoureyes.databinding.ActivityHowToBinding

class AppFirstActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityHowToBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if(isAppFirstExecute()) {

            var pageIdx = 0
            val pageLastIdx = 8

            binding = ActivityHowToBinding.inflate(layoutInflater)
            setContentView(binding.root)
            // 첫 페이지 세팅
            binding.descriptionImage.setImageResource(R.drawable.onboarding1)
            binding.prevBtn.isEnabled = false // 이전 버튼 비활성화


            // 이전 버튼
            binding.prevBtn.setOnClickListener {
                if(pageIdx > 0) {
                    if(pageIdx == pageLastIdx) {
                        binding.nextBtn.setText("다음")
                    }
                    pageIdx -= 1
                    if(pageIdx == 0) binding.prevBtn.isEnabled = false // 첫 페이지면 이전 버튼 비활성화

                    binding.descriptionImage.setImageResource(
                        resources.getIdentifier(
                            "@drawable/onboarding${pageIdx+1}",
                            "drawable",
                            packageName)
                    )
                }
            }

            // 다음 버튼
            binding.nextBtn.setOnClickListener {
                if(pageIdx == pageLastIdx ) { // 마지막 페이지면
                    startActivity(Intent(this, HomeActivity::class.java))

                }else {
                    binding.prevBtn.isEnabled = true // 이전 버튼 활성화
                    pageIdx += 1

                    if(pageIdx == pageLastIdx) {
                        binding.nextBtn.setText("앱 시작하기")
                    }

                    binding.descriptionImage.setImageResource(
                        resources.getIdentifier(
                            "@drawable/onboarding${pageIdx+1}",
                            "drawable",
                            packageName)
                    )

                }
            }



        }else{

            startActivity(Intent(this, HomeActivity::class.java))
        }

    }


    //앱최초실행확인 (true-최초실행)
    fun isAppFirstExecute() : Boolean {
        val pref : SharedPreferences = getSharedPreferences("IsFirst", Activity.MODE_PRIVATE)
        val isSecond : Boolean = pref.getBoolean("isSecond", false);
        if(!isSecond){
            // 최초 실행시 true 저장
            val editor = pref.edit()
            editor.putBoolean("isSecond", true)
            editor.commit()
        }
        return !isSecond
    }


}