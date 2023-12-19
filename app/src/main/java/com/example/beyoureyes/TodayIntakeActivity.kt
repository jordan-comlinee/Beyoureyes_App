package com.example.beyoureyes

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TodayIntakeActivity : AppCompatActivity() {
    private val calorieList : ArrayList<Int> = arrayListOf()
    private var totalNa : Int = 0
    private var totalTransfat : Int = 0
    private var totalSatfat : Int = 0
    private var totalFat : Int = 0
    private var totalCarb : Int = 0
    private var totalChole : Int = 0
    private var totalSuger : Int = 0
    private var totalProt : Int = 0
    private var totalCalorie : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_intake)

        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)    // 화면 전환 시 애니메이션

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


        //toolBar
        val toolBar = findViewById<Toolbar>(R.id.toolbarDefault)
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        val toolbarBackButton = findViewById<ImageButton>(R.id.toolbarBackBtn)
        setSupportActionBar(toolBar)
        //Toolbar에 앱 이름 표시 제거!!
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle.setText("오늘의 영양소 확인")

        toolbarBackButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            //overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_enter)
        }


        db.collection("userIntakeNutrition")
            .whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("TODAYINTAKE", "${document.id} => ${document.data}")
                    val nutritionMap = document.data["nutrition"] as? Map<String, Long>
                    val calories = document.data.get("calories") as Long
                    if (nutritionMap != null) {
                        /*
                        val saturatedFat = nutritionMap["saturatedFat"]
                        val carbs = nutritionMap["carbs"]
                        val protein = nutritionMap["protein"]
                        val fat = nutritionMap["fat"]
                        val transFat = nutritionMap["transFat"]
                        val cholesterol = nutritionMap["cholesterol"]
                        val natrium = nutritionMap["natrium"]
                        val sugar = nutritionMap["sugar"]

                        // Null 체크 후 사용
                        if (saturatedFat != null) totalSatfat += saturatedFat.toInt()
                        if (carbs != null) totalCarb += carbs.toInt()
                        if (protein != null) totalProt += protein.toInt()
                        if (fat != null) totalFat += fat.toInt()
                        if (transFat != null) totalTransfat += transFat.toInt()
                        if (cholesterol != null) totalChole += cholesterol.toInt()
                        if (natrium != null) totalNa += natrium.toInt()
                        if (sugar != null) totalSuger += sugar.toInt()

                         */
                    }
                    // Firestore에서 가져온 질환 정보 입력
                    if (calories != null) {
                        //calorieList.add(calories.toInt())
                        totalCalorie += calories.toInt()
                    }
                }
                totalCalorieTextView.setText("${totalCalorie.toString()}kcal")
                satFatPer.setText("${totalSatfat}%")
                fatPer.setText("${totalFat}%")
                carPer.setText("${totalCarb}%")
                proPer.setText("${totalProt}%")
                choPer.setText("${totalChole}%")
                sugerPer.setText("${totalSuger}%")
                naPer.setText("${totalNa}%")
            }
            .addOnFailureListener { exception ->
                Log.w("TODAYINTAKE", "Error getting documents.", exception)
            }

    }
}