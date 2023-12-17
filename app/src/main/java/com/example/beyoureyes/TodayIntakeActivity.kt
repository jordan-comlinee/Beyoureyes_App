package com.example.beyoureyes

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TodayIntakeActivity : AppCompatActivity() {

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

    }
}