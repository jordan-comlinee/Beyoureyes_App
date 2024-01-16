package com.dna.beyoureyes

import TTSManager
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import java.util.Locale
import com.dna.beyoureyes.databinding.ActivityFoodInfoNutritionPersonalizedBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date

class FoodInfoNutritionPersonalizedActivity : AppCompatActivity() {

    private lateinit var ttsManager: TTSManager
    private lateinit var speakButton: Button
    private val camera = Camera()
    private lateinit var binding: ActivityFoodInfoNutritionPersonalizedBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodInfoNutritionPersonalizedBinding.inflate(layoutInflater)
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
        val eatButton = binding.buttoneat

        // intent로 전달받은 식품 정보 파싱
        val totalKcal = intent.getIntExtra("totalKcal", 0)

        val nutriFactsInMilli = intent.getIntegerArrayListExtra("nutriFactsInMilliString")
        nutriFactsInMilli?.let {
            Log.d("test", it.joinToString())
        }

        // 영양성분 정보 객체 생성
        val nutriFacts = NutritionFacts(nutriFactsInMilli!!.toIntArray(), totalKcal)
        Log.d("test", nutriFacts.toString())

        // 에너지 섭취 비율 원형 차트
        val chart = binding.pieChart
        val energyChart = EnergyChart(chart)
        nutriFacts.carbs?.let { carbs ->
            nutriFacts.protein?.let { protein ->
                nutriFacts.fat?.let { fat -> // 탄단지 객체 null safe 처리

                    // 탄단지 에너지값 설정
                    energyChart.setCaloreisFromMilliGram(
                        carbs.getMilliGram(),
                        protein.getMilliGram(),
                        fat.getMilliGram()
                    )

                    // 차트 표시 설정
                    energyChart.setChart(this)
                }
            }
        }

        // 칼로리 표시
        val calorieTextView = binding.kcaltextview
        calorieTextView.text = "${totalKcal}kcal"

        // 영양성분 표시 ----------------------------------------
        val cautionTextView = binding.nutricaution
        val line0 = binding.line0

        val lineViewsList = arrayListOf<PercentOfDailyValueLineView>(
            PercentOfDailyValueLineView(
                binding.line1Label, binding.line1Percent),
            PercentOfDailyValueLineView(
                binding.line2Label, binding.line2Percent),
            PercentOfDailyValueLineView(
                binding.line3Label, binding.line3Percent),
            PercentOfDailyValueLineView(
                binding.line4Label, binding.line4Percent),
            PercentOfDailyValueLineView(
                binding.line5Label, binding.line5Percent),
            PercentOfDailyValueLineView(
                binding.line6Label, binding.line6Percent),
            PercentOfDailyValueLineView(
                binding.line7Label, binding.line7Percent)
        )

        val percentView = PercentViewOfNutritionFacts(cautionTextView, line0, lineViewsList)

        // 사용자 맞춤 권장량 계산
        val userDVs = AppUser.info?.getDailyValues()

        // 권장량 대비 영양소 함유 퍼센트 표시 설정
        AppUser.info?.disease?.let { disease -> // 사용자가 질환 있을 시
            percentView.setWarningText(disease) // 경고 문구 설정
            percentView.setLineViews(this,
                nutriFacts, userDVs, AppUser.info!!.getNutrisToCare())
        }?:run{ // 질환 없을 시
            percentView.hideWarningText() // 경고 문구 없애기
            percentView.setLineViews(nutriFacts, userDVs)
        }

        // 모든 정보 표시 버튼
        val btnGeneral = binding.buttonGeneralize

        btnGeneral.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.none, R.anim.none)
        }

        binding.buttonRetry.setOnClickListener {
            while(camera.start(this) == -1){
                camera.start(this)
            }
        }


        // 버튼 초기화
        speakButton = binding.buttonVoice

        // TTSManager 초기화 완료되었을때
        ttsManager = TTSManager(this) {
            // 버튼 눌렀을 때 TTS 실행
            speakButton.setOnClickListener {
                val calorieText = "칼로리는 ${totalKcal}kcal 입니다."
                val nutrientsText = buildString {
                    for (i in lineViewsList.indices) {
                        val nutrientName =
                            lineViewsList[i].labelTextView.text.toString().removePrefix("ㄴ")
                        val nutrientPercent = lineViewsList[i].percentTextView.text.toString()
                        append("$nutrientName 은 $nutrientPercent")

                        if (i < lineViewsList.size - 1) {
                            append(", ")
                        }
                    }
                }

                val textToSpeak =
                    "당신의 맞춤별 영양 정보를 분석해드리겠습니다. 해당 식품의 $calorieText 또한 영양 성분 정보는 당신의 일일 권장량 당 $nutrientsText 입니다." +
                            " 알레르기 정보는 인식되지 않았습니다. 추가적인 정보를 원하시면 화면에 다시 찍기 버튼을 눌러주세요. " +
                            "또한 해당 식품 섭취 시 먹기 버튼을 클릭하고 먹은 양의 정보를 알려주세요."
                ttsManager.speak(textToSpeak)
            }
        }

        //eatButton
        val modifiedKcalList = intArrayOf(300)
        val moPercentList = intArrayOf(1, 2, 3)
        val koreanCharacterList = arrayOf("1", "2", "3")

        binding.buttoneat.setOnClickListener{
            val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_alert_dialog_intake, null)

            val builder = AlertDialog.Builder(this@FoodInfoNutritionPersonalizedActivity)
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

            Log.d("nutriList", moPercentList.toString())
            Log.d("nutriList", koreanCharacterList.toString())

            buttonBack.setOnClickListener {
                alertDialog.dismiss()
            }

            // 바 차트의 데이터 설정
            val entries = arrayListOf<BarEntry>()
            entries.add(BarEntry(0f, 0f))
            applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)


            buttonAll.setOnClickListener {
                buttonAll.setBackgroundResource(R.drawable.button_highlight)
                buttonAll.setTextColor(ContextCompat.getColor(this, R.color.white))
                buttonLot.setBackgroundResource(R.drawable.button_default)
                buttonLot.setTextColor(ContextCompat.getColor(this, R.color.black))
                buttonHalf.setBackgroundResource(R.drawable.button_default)
                buttonHalf.setTextColor(ContextCompat.getColor(this, R.color.black))
                buttonLittle.setBackgroundResource(R.drawable.button_default)
                buttonLittle.setTextColor(ContextCompat.getColor(this, R.color.black))
                ratio = 1.0
                if (entries.isNotEmpty()) entries.clear()
                entries.add(BarEntry(0f, 100f))
                applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)
                //Toast.makeText(this@FoodInfoAllActivity, ratio.toString(), Toast.LENGTH_LONG).show()
            }
            buttonLot.setOnClickListener {
                buttonAll.setBackgroundResource(R.drawable.button_default)
                buttonAll.setTextColor(ContextCompat.getColor(this, R.color.black))
                buttonLot.setBackgroundResource(R.drawable.button_highlight)
                buttonLot.setTextColor(ContextCompat.getColor(this, R.color.white))
                buttonHalf.setBackgroundResource(R.drawable.button_default)
                buttonHalf.setTextColor(ContextCompat.getColor(this, R.color.black))
                buttonLittle.setBackgroundResource(R.drawable.button_default)
                buttonLittle.setTextColor(ContextCompat.getColor(this, R.color.black))
                ratio = 0.75
                if (entries.isNotEmpty()) entries.clear()
                entries.add(BarEntry(0f, 75f))
                applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)
                //Toast.makeText(this@FoodInfoAllActivity, ratio.toString(), Toast.LENGTH_LONG).show()
            }
            buttonHalf.setOnClickListener {
                buttonAll.setBackgroundResource(R.drawable.button_default)
                buttonAll.setTextColor(ContextCompat.getColor(this, R.color.black))
                buttonLot.setBackgroundResource(R.drawable.button_default)
                buttonLot.setTextColor(ContextCompat.getColor(this, R.color.black))
                buttonHalf.setBackgroundResource(R.drawable.button_highlight)
                buttonHalf.setTextColor(ContextCompat.getColor(this, R.color.white))
                buttonLittle.setBackgroundResource(R.drawable.button_default)
                buttonLittle.setTextColor(ContextCompat.getColor(this, R.color.black))
                ratio = 0.5
                if (entries.isNotEmpty()) entries.clear()
                entries.add(BarEntry(0f, 50f))
                applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)
                //Toast.makeText(this@FoodInfoAllActivity, ratio.toString(), Toast.LENGTH_LONG).show()
            }
            buttonLittle.setOnClickListener {
                buttonAll.setBackgroundResource(R.drawable.button_default)
                buttonAll.setTextColor(ContextCompat.getColor(this, R.color.black))
                buttonLot.setBackgroundResource(R.drawable.button_default)
                buttonLot.setTextColor(ContextCompat.getColor(this, R.color.black))
                buttonHalf.setBackgroundResource(R.drawable.button_default)
                buttonHalf.setTextColor(ContextCompat.getColor(this, R.color.black))
                buttonLittle.setBackgroundResource(R.drawable.button_highlight)
                buttonLittle.setTextColor(ContextCompat.getColor(this, R.color.white))
                ratio = 0.25
                if (entries.isNotEmpty()) entries.clear()
                entries.add(BarEntry(0f, 25f))
                applyBarChart(horizontalChartIntake, entries, "#FF0000", 100f)
                //Toast.makeText(this@FoodInfoAllActivity, ratio.toString(), Toast.LENGTH_LONG).show()
            }

            buttonSend.setOnClickListener {
                if (moPercentList != null) {
                    val nutriData: HashMap<String, Serializable> = hashMapOf(
                        "userID" to AppUser.id!!,
                        "date" to SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()),
                        "calories" to modifiedKcalList!!.joinToString(", ").toInt() * ratio
                    )
                    for (i in koreanCharacterList.indices) {
                        nutriData[koreanCharacterList[i]] = moPercentList[i].toInt() * ratio
                    }


                    //Toast.makeText(this@FoodInfoAllActivity, sendData.toString(), Toast.LENGTH_LONG).show()
                    sendData(nutriData, "userIntakeNutrition")
                    alertDialog.dismiss()
                }
            }

            alertDialog.show()
        }//eatButton

    }

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