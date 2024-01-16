package com.dna.beyoureyes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.dna.beyoureyes.databinding.ActivityNutriCautionBinding

class NutriCautionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNutriCautionBinding
    private lateinit var SecondButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNutriCautionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "영양 정보 촬영하기"

        binding.include.toolbarBackBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        SecondButton = binding.buttonsecond

        SecondButton.setOnClickListener {
            val intent = Intent(this, CameraFirstActivity::class.java)
            startActivity(intent)
        }
    }
}