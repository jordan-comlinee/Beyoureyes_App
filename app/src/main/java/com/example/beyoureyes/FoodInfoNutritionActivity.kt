package com.example.beyoureyes

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class FoodInfoNutritionActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private val camera = Camera()
    val nutri = listOf("나트륨", "탄수화물", "ㄴ당류", "지방", "ㄴ포화지방", "콜레스테롤", "단백질")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_nutrition)

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
        speakButton = findViewById(R.id.button5)



        // 칼로리 intent 하여 kcalTextView에 표시
        val kcalText: TextView = findViewById(R.id.textView5)
        val modifiedKcalList = intent.getStringArrayListExtra("modifiedKcalListText")
        val Percent = intent.getStringArrayListExtra("PercentList")

        if (modifiedKcalList != null) {
            kcalText.text = modifiedKcalList.joinToString(", ") + " kcal"
        }

        // 원형 차트 (영양성분 이름  + 해당 g) intent해서 표시
        val chart = findViewById<PieChart>(R.id.pieChartScanSuccess)
        chart.setUsePercentValues(true)
        val entries = ArrayList<PieEntry>()
        // NutriActivity 에서 데이터 받기
        val moPercentList = intent.getStringArrayListExtra("modifiedPercentList")
        val koreanCharacterList = listOf("나트륨", "탄수화물", "당류", "지방", "포화지방", "콜레스테롤", "단백질")

        // 단백질, 탄수화물, 지방
        if (moPercentList != null) {

            entries.add(PieEntry(moPercentList[1].toFloat(), koreanCharacterList[1]))
            entries.add(PieEntry(moPercentList[3].toFloat(), koreanCharacterList[3]))
            entries.add(PieEntry(moPercentList[6].toFloat(), koreanCharacterList[6]))
        }
        // 차트 색깔
        val colors = listOf(
            Color.parseColor("#C2FF00"),
            Color.parseColor("#F1BC00"),
            Color.parseColor("#FFC2E5")
        )

        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.apply {
            // Piechart 속 파이들 색상 설정
            setColors(colors)
            // 값(백분율)에 대한 색상 설정
            valueTextColor = Color.BLACK
            // 값에 대한 크기 설정
            valueTextSize = 10f
        }

        val pieData = PieData(pieDataSet)
        // 값에 사용자 정의 형식(백분율 값 + "%") 설정
        pieDataSet.valueFormatter = object : ValueFormatter() { // 값을 차트에 어떻게 표시할지
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%" // 값을 정수 형식으로 표시
            }
        }

        chart.apply {
            data = pieData
            description.isEnabled = false // 차트 설명 비활성화
            isRotationEnabled = false // 차트 회전 활성화
            legend.isEnabled = false // 하단 설명 비활성화
            isDrawHoleEnabled = true // 가운데 빈 구멍 활성화 비활성화 여부
            holeRadius = 20f // 가운데 빈 구멍 크기
            transparentCircleRadius = 40f // 투명한 부분 크기
            centerText = null // 가운데 텍스트 없앰
            setEntryLabelColor(Color.BLACK) // label 색상
            animateY(1400, Easing.EaseInOutQuad) // 1.4초 동안 애니메이션 설정
            animate()
        }

        // 버튼 눌렀을 때 TTS 실행
        speakButton.setOnClickListener {
            val calorieText = "칼로리는 $modifiedKcalList 입니다."
            val nutrientsText = buildString {
                for (i in koreanCharacterList.indices) {
                    append("${koreanCharacterList[i]}은 ${moPercentList?.get(i)}g")
                    if (i < koreanCharacterList.size - 1) {
                        append(", ")
                    }
                }
            }

            val textToSpeak = "안녕하세요! 영양 정보를 분석해드리겠습니다. 해당식품의 $calorieText 또한 영양 성분 정보는 $nutrientsText 입니다. 다른 영양 성분 정보는 인식되지 않았습니다. 추가적인 정보를 원하시면 화면에 다시 찍기 버튼을 눌러주세요."
            speak(textToSpeak)
        }

        // Percent 리스트의 크기
        val percentSize = Percent?.size ?: 0

        // 각각의 line_percent TextView에 Percent 리스트의 값을 설정
        for (i in 0 until percentSize) {
            val percentTextView = findViewById<TextView>(resources.getIdentifier("line${i + 1}_percent", "id", packageName))
            val percentValue = Percent?.get(i) ?: "N/A"
            percentTextView.text = "$percentValue%"
        }

        val nutriSize = nutri?.size ?: 0

        for (i in 0 until nutriSize) {
            val nutriTextView = findViewById<TextView>(resources.getIdentifier("line${i + 1}_label", "id", packageName))
            val nutriValue = nutri?.get(i) ?: "N/A"
            nutriTextView.text = "$nutriValue"
        }

        val retryButton = findViewById<Button>(R.id.buttonRetry)

        retryButton.setOnClickListener {
            while(camera.start(this) == -1){
                camera.start(this)
            }
        }

        // 맞춤 정보 버튼
        val personalButton = findViewById<Button>(R.id.buttonPersonalized)

        // Firebase에서 사용자 정보 가져오기
        // Firebase 연결을 위한 설정값
        val userIdClass = application as userId
        val userId = userIdClass.userId
        val db = Firebase.firestore

        // 유저 정보 받아오기
        db.collection("userInfo")
            .whereEqualTo("userID", userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    var user:UserInfo? = null

                    // 유저 정보가 이미 존재하는 경우
                    if (result != null && !result.isEmpty) {
                        for (document in result) {
                            Log.d("FIRESTORE : ", "${document.id} => ${document.data}")
                            user = UserInfo.parseFirebaseDoc(document)

                            if (user!=null) {
                                Log.d("FIRESTORE : ", "got UserInfo")
                                break
                            }
                        }
                    }

                    user?.let { u -> // 사용자 정보 있을 시

                        val intent = Intent(this, FoodInfoNutritionPersonalizedActivity::class.java)
                        // 식품 정보 전달
                        intent.putExtra("totalKcal", modifiedKcalList?.get(0)?.toInt())
                        intent.putExtra("nutriFactsInMilliString",
                            ArrayList(moPercentList?.map {it.toInt()}))
                        // 사용자 정보 전달
                        intent.putExtra("userAge", u.age)
                        intent.putExtra("userSex", u.gender)
                        intent.putExtra("userDisease", u.disease)
                        intent.putExtra("userAllergic", u.allergic)

                        personalButton.setOnClickListener {
                            startActivity(intent)
                            overridePendingTransition(R.anim.none, R.anim.none)
                        }

                    } ?: run {// 사용자 정보 없을 시
                        personalButton.isEnabled = false // 버튼 비활성화
                        personalButton.setBackgroundResource(R.drawable.button_grey) // 비활성화 drawable 추가함
                    }

                } else {
                    // 쿼리 중에 예외가 발생한 경우
                    Log.d("FIRESTORE : ", "Error getting documents.", task.exception)
                    personalButton.isEnabled = false // 버튼 비활성화
                    personalButton.setBackgroundResource(R.drawable.button_grey) // 비활성화 drawable 추가함

                }
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
}