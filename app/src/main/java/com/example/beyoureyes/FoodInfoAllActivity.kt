package com.example.beyoureyes

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.util.Locale

class FoodInfoAllActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speakButton: Button
    private lateinit var personalButton: Button

    val nutri = listOf("나트륨", "탄수화물", "ㄴ당류", "지방", "ㄴ포화지방", "콜레스테롤", "단백질")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info_all)

        val eatButton : Button = findViewById(R.id.eatButton)

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
        val modifiedKcalList = intent.getStringArrayListExtra("modifiedKcalListText")
        val Percent = intent.getStringArrayListExtra("PercentList")
        val moPercentList = intent.getStringArrayListExtra("modifiedPercentList")
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

                allergyChipGroup.addView(chip)
            }
        }

        // 칼로리 intent 하여 kcalTextView에 표시
        val kcalText: TextView = findViewById(R.id.textView5)

        if (modifiedKcalList != null) {
            kcalText.text = modifiedKcalList.joinToString(", ") + " kcal"
        }

        // 원형 차트 (영양성분 이름  + 해당 g) intent해서 표시
        val chart = findViewById<PieChart>(R.id.pieChartScanSuccess)
        chart.setUsePercentValues(true)
        val entries = ArrayList<PieEntry>()
        // LoadingActivity 에서 데이터 받기
        val koreanCharacterList = listOf("나트륨", "탄수화물", "당류", "지방", "포화지방", "콜레스테롤", "단백질")

        // 단백질, 탄수화물, 지방
        if (Percent != null) {

            entries.add(PieEntry(Percent[1].toFloat(), koreanCharacterList[1]))
            entries.add(PieEntry(Percent[3].toFloat(), koreanCharacterList[3]))
            entries.add(PieEntry(Percent[6].toFloat(), koreanCharacterList[6]))
        }

        // 색깔 적용
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
            valueTextSize = 20f
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
            chart.setEntryLabelTextSize(20f)
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
        // 버튼 눌렀을 때 TTS 실행 -> 수정예정
        speakButton.setOnClickListener {
            val calorieText = "칼로리는 $modifiedKcalList 입니다."
            val nutrientsText = buildString {
                for (i in koreanCharacterList.indices) {
                    append("${koreanCharacterList[i]}은 ${Percent?.get(i)}%")
                    if (i < koreanCharacterList.size - 1) {
                        append(", ")
                    }
                }
            }

            val allergyText = "해당 식품에는 ${allergyList?.joinToString(", ")}가 함유되어 있습니다."


            val textToSpeak = "영양 정보를 분석해드리겠습니다. $allergyText $calorieText 또한 영양 성분 정보는 일일 권장량 당 $nutrientsText 입니다."
            speak(textToSpeak)
        }

        // Percent 리스트의 크기
        val percentSize = Percent?.size ?: 0

        // 각각의 line_percent TextView에 Percent 리스트의 값 적용
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

        //eatButton

        eatButton.setOnClickListener{
            val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_alert_dialog_intake, null)

            val builder = AlertDialog.Builder(this@FoodInfoAllActivity)
            var ratio : Double = 0.0
            builder.setView(dialogView)
            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val buttonAll : Button = dialogView.findViewById(R.id.buttonAll)
            val buttonLot : Button = dialogView.findViewById(R.id.buttonLot)
            val buttonHalf : Button = dialogView.findViewById(R.id.buttonHalf)
            val buttonLittle : Button = dialogView.findViewById(R.id.buttonLittle)

            val buttonBack : Button = dialogView.findViewById(R.id.buttonBack)
            val buttonSend : Button = dialogView.findViewById(R.id.buttonSend)

            val horizontalChartIntake : BarChart = dialogView.findViewById(R.id.horizontalChartIntake)

            buttonBack.setOnClickListener {
                alertDialog.dismiss()
            }

            // 바 차트의 데이터 설정
            val entries = arrayListOf<BarEntry>()
            entries.add(BarEntry(0f, 0f))
            applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)

            moPercentList?.add("1")
            moPercentList?.add("2")
            moPercentList?.add("3")


            buttonAll.setOnClickListener {
                ratio = 1.0
                entries.add(BarEntry(0f, 100f))
                applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)
                Toast.makeText(this@FoodInfoAllActivity, ratio.toString(), Toast.LENGTH_LONG).show()
            }
            buttonLot.setOnClickListener {
                ratio = 0.75
                entries.add(BarEntry(0f, 75f))
                applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)
                Toast.makeText(this@FoodInfoAllActivity, ratio.toString(), Toast.LENGTH_LONG).show()
            }
            buttonHalf.setOnClickListener {
                ratio = 0.5
                entries.add(BarEntry(0f, 50f))
                applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)
                Toast.makeText(this@FoodInfoAllActivity, ratio.toString(), Toast.LENGTH_LONG).show()
            }
            buttonLittle.setOnClickListener {
                ratio = 0.25
                entries.add(BarEntry(0f, 25f))
                applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)
                Toast.makeText(this@FoodInfoAllActivity, ratio.toString(), Toast.LENGTH_LONG).show()
            }

            buttonSend.setOnClickListener {
                if (moPercentList != null) {
                    // "나트륨", "탄수화물", "당류", "지방", "포화지방", "콜레스테롤", "단백질"
                    val sendData = moPercentList.map { it.toFloat() * ratio }

                    val nutriData = hashMapOf(
                        "calories" to modifiedKcalList?.joinToString(", "),
                        "natrium" to sendData[0],
                        "carbs" to sendData[1],
                        "sugar" to sendData[2],
                        "fat" to sendData[3],
                        "saturatedFat" to sendData[4],
                        "cholesterol" to sendData[5],
                        "protein" to sendData[6]
                    )

                    Toast.makeText(this@FoodInfoAllActivity, sendData.toString(), Toast.LENGTH_LONG).show()
                    //sendData(nutriData, "userIntakeNutrition")
                }
            }

            alertDialog.show()
        }//alertDialog


        // personal Button
        personalButton = findViewById(R.id.buttonPersonalized)

        // 사용자 맞춤 서비스 제공 여부 검사(맞춤 정보 있는지)
        // 기존 Firebase와의 통신 코드는 다 제거
        AppUser.info?.let { // 사용자 정보 있을 시

            val intent = Intent(this, FoodInfoAllPersonalizedActivity::class.java) //OCR 실패시 OCR 가이드라인으로 이동
            // 식품 정보 전달
            intent.putExtra("totalKcal", modifiedKcalList?.get(0)?.toInt())
            intent.putExtra("nutriFactsInMilliString",
                ArrayList(moPercentList?.map {it.toInt()}))
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

    } //onCreate

    private fun applyBarChart(barChart: BarChart, entries: List<BarEntry>, color: String, maximum: Float) {
        // 바 차트의 데이터셋 생성
        val dataSet = BarDataSet(entries, "My Data")
        dataSet.color = Color.parseColor(color)

        dataSet.setDrawValues(false);

        // 바 차트의 X축 레이블 설정
        val labels = arrayListOf<String>()
        labels.add("Label 1")

        // 바 차트의 X축, Y축 설정
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.setDrawGridLines(false)
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE)
        xAxis.setEnabled(false)
        xAxis.setDrawAxisLine(false)

        val yLeft = barChart.axisLeft
        //Set the minimum and maximum bar lengths as per the values that they represent
        yLeft.axisMaximum = maximum
        yLeft.axisMinimum = 0f
        yLeft.isEnabled = false

        // 바 차트의 데이터 설정
        val data = BarData(dataSet)
        barChart.data = data

        // 바 차트의 다양한 설정 (예시)
        //barChart.setOnChartValueSelectedListener(null) // 클릭 이벤트 비활성화
        barChart.description.isEnabled = false  // 설명 삭제
        barChart.setPinchZoom(false)
        barChart.setDrawValueAboveBar(false) // 위에 값 표시 삭제
        barChart.legend.isEnabled = false // 레전드 삭제
        barChart.description.isEnabled = false // 차트의 설명 비활성화
        barChart.setDrawGridBackground(false) // 그리드 배경 비활성화
        barChart.axisLeft.isEnabled = false // 왼쪽 Y축 비활성화
        barChart.axisRight.isEnabled = false // 오른쪽 Y축 비활성화
        barChart.legend.isEnabled = false // 범례 비활성화
        barChart.barData.barWidth = 100f // 바 차트 두께 설정 (1.0 이 디폴트)
        barChart.isDoubleTapToZoomEnabled = false // 더블 클릭 시 비활성화
        barChart.setPinchZoom(false)


        barChart.animateY(1000)


        // 레이아웃 파라미터 설정 (예시)
        val layoutParams = barChart.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT



        // 배경에 둥근 모서리 적용 (예시)
        //barChart.setBackgroundResource(R.drawable.rounded_corner_horizontal_barchart)

        // 바 차트 갱신
        barChart.layoutParams = layoutParams
        barChart.invalidate()
    } // applyBarChart

    private fun sendData(foodInfo: HashMap<String, Serializable>, collectionName: String){
        val db = Firebase.firestore
        db.collection(collectionName)
            .add(foodInfo)
            .addOnSuccessListener { documentReference ->
                Log.d("REGISTERFIRESTORE :", "SUCCESS added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("REGISTERFIRESTORE :", "Error adding document", e)
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