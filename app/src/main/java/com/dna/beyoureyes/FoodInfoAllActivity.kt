package com.dna.beyoureyes

import TTSManager
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.dna.beyoureyes.databinding.ActivityFoodInfoAllBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodInfoAllActivity : AppCompatActivity() {

    private lateinit var ttsManager: TTSManager
    private lateinit var speakButton: Button
    private lateinit var personalButton: Button
    private lateinit var binding: ActivityFoodInfoAllBinding

    val nutri = listOf("나트륨", "탄수화물", " ㄴ당류", "지방", " ㄴ포화지방", "콜레스테롤", "단백질")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodInfoAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 먹기 버튼
        val eatButton: Button = binding.buttoneat

        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "영양 분석 결과"

        binding.include.toolbarBackBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_enter)
        }

        // 알러지 정보 intent하여 표시
        val allergyChipGroup: ChipGroup = binding.allergyChipGroup
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

                chip.setPadding(20, 20, 20, 20) // 상, 좌, 하, 우 패딩

                allergyChipGroup.addView(chip)
            }
        }

        // 칼로리 intent 하여 kcalTextView에 표시
        val kcalText: TextView = binding.kcaltextview

        if (modifiedKcalList != null) {
            kcalText.text = modifiedKcalList.joinToString(", ") + " kcal"
        }

        // 원형 차트 (영양성분 이름  + 해당 g) intent해서 표시

        // LoadingActivity 에서 데이터 받기
        val koreanCharacterList = listOf("나트륨", "탄수화물", "당류", "지방", "포화지방", "콜레스테롤", "단백질")

        // 영양성분 정보 객체 생성
        val kcal = modifiedKcalList!!.get(0).toInt()
        val nutriFactsInMilli = moPercentList?.map { it -> it.toInt() }
        val nutriFacts = NutritionFacts(nutriFactsInMilli!!.toIntArray(), kcal)

        // 에너지 섭취 비율 원형 차트
        val chart: PieChart = binding.pieChart
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


        speakButton = binding.buttonVoice
        ttsManager = TTSManager(this) {
            // 버튼 눌렀을 때 TTS 실행
            speakButton.setOnClickListener {
                val calorieText = "칼로리는 $modifiedKcalList kcal 입니다."
                val nutrientsText = buildString {
                    for (i in koreanCharacterList.indices) {
                        append("${koreanCharacterList[i]}은 ${Percent?.get(i)}%")
                        if (i < koreanCharacterList.size - 1) {
                            append(", ")
                        }
                    }
                }


                val allergyText = "해당 식품에는 ${allergyList?.joinToString(", ")}가 함유되어 있습니다."


                val textToSpeak =
                    "영양 정보를 분석해드리겠습니다. $allergyText $calorieText 또한 영양 성분 정보는 1일 영양성분 기준치 당 $nutrientsText 입니다." +
                            " 해당 식품 섭취 시 먹기 버튼을 클릭하고 먹은 양의 정보를 알려주세요."
                ttsManager.speak(textToSpeak)
            }
        }

        // Percent 리스트의 크기
        val percentSize = Percent?.size ?: 0

        // 각각의 line_percent TextView에 Percent 리스트의 값 적용
        for (i in 0 until percentSize) {
            val percentTextView = findViewById<TextView>(
                resources.getIdentifier(
                    "line${i + 1}_percent",
                    "id",
                    packageName
                )
            )
            val percentValue = Percent?.get(i) ?: "N/A"
            percentTextView.text = "$percentValue%"
        }

        val nutriSize = nutri?.size ?: 0

        for (i in 0 until nutriSize) {
            val nutriTextView = findViewById<TextView>(
                resources.getIdentifier(
                    "line${i + 1}_label",
                    "id",
                    packageName
                )
            )
            val nutriValue = nutri?.get(i) ?: "N/A"
            nutriTextView.text = "$nutriValue"
        }

        //eatButton
        eatButton.setOnClickListener {
            val dialogView =
                LayoutInflater.from(this).inflate(R.layout.activity_alert_dialog_intake, null)

            val builder = AlertDialog.Builder(this@FoodInfoAllActivity)
            var ratio: Double = 0.0
            builder.setView(dialogView)
            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val buttonAll: Button = dialogView.findViewById(R.id.buttonAll)
            val buttonLot: Button = dialogView.findViewById(R.id.buttonLot)
            val buttonHalf: Button = dialogView.findViewById(R.id.buttonHalf)
            val buttonLittle: Button = dialogView.findViewById(R.id.buttonLittle)

            val buttonBack: Button = dialogView.findViewById(R.id.buttonBack)
            val buttonSend: Button = dialogView.findViewById(R.id.buttonSend)

            val horizontalChartIntake: BarChart =
                dialogView.findViewById(R.id.horizontalChartIntake)

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
                    Toast.makeText(this@FoodInfoAllActivity, "먹은 양이 저장되었어요.", Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }
            }

            alertDialog.show()
        }

        // personal Button
        personalButton = binding.buttonPersonalized

        // 사용자 맞춤 서비스 제공 여부 검사(맞춤 정보 있는지)
        // 기존 Firebase와의 통신 코드는 다 제거
        AppUser.info?.let { // 사용자 정보 있을 시

            val intent =
                Intent(this, FoodInfoAllPersonalizedActivity::class.java) //OCR 실패시 OCR 가이드라인으로 이동
            // 식품 정보 전달
            intent.putExtra("totalKcal", modifiedKcalList?.get(0)?.toInt())
            intent.putExtra("nutriFactsInMilliString",
                ArrayList(moPercentList?.map { it.toInt() })
            )
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

    override fun onBackPressed() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun applyBarChart(
        barChart: BarChart,
        entries: List<BarEntry>,
        color: String,
        maximum: Float
    ) {
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

    private fun sendData(foodInfo: HashMap<String, Serializable>, collectionName: String) {
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
}
