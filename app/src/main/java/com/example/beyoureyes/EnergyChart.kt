package com.example.beyoureyes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.ResourcesCompat.getColorStateList
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

//========================================================================
// 오늘의 섭취량 통계 디스플레이 요소 관련 클래스들
//========================================================================

// primary 생성자 - 액티비티 내 원형 차트 객체를 전달
class EnergyChart(private val chart : PieChart) {

    // =========================================================================
    // 생성자
    // =========================================================================

    // primary 생성자 - 탄단지 칼로리 값은 일단 0으로 초기화
    private var carbsCal = 0f
    private var proteinCal = 0f
    private var fatCal = 0f

    init {
        chart.setUsePercentValues(true)
        chart.setNoDataText("데이터가 없어 차트를 표시할 수 없어요.") // 차트 표시 실패할 때 안내 문구
    }

    // =========================================================================
    // set 메소드
    // =========================================================================

    fun hide(){
        chart.visibility = View.GONE
    }

    fun setNoDataText(text:String){
        chart.setNoDataText(text)
    }

    fun setCaloreis(carbs: Float, protein: Float, fat: Float ){
        carbsCal = carbs
        proteinCal = protein
        fatCal = fat
    }

    fun setCaloreisFromMilliGram(carbs: Int, protein: Int, fat: Int){
        // mg -> g 변경 후 영양소별 1g당 열량 곱하기
        carbsCal = (carbs.toFloat() / 1000) * 4
        proteinCal = (protein.toFloat() / 1000) * 4
        fatCal = (fat.toFloat() / 1000) * 9
    }

    fun setCaloreisFromGram(carbs: Int, protein: Int, fat: Int){
        // 영양소별 1g당 열량 곱하기
        carbsCal = carbs.toFloat() * 4
        proteinCal = protein.toFloat() * 4
        fatCal = fat.toFloat() * 9
    }

    // 앱 전체에서 쓰는 색상 정의 xml 활용하기 위해 context 전달 필요.
    // 액티비티 내에서 쓸 때 this로 context 전달하면 됨. ex) setChart(this)
    fun setChart(context: Context) {
        // 차트 표기 -----------------------------------------------------------------
        // data set - 단백질, 탄수화물, 지방 에너지 비율 구하기
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(carbsCal, "탄수화물"))
        entries.add(PieEntry(proteinCal, "단백질"))
        entries.add(PieEntry(fatCal, "지방"))

        // 색깔 적용
        val colors = listOf(
            ContextCompat.getColor(context, R.color.chartgreen),
            ContextCompat.getColor(context, R.color.chartyellow),
            ContextCompat.getColor(context, R.color.chartpink)
        )

        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.apply {
            setColors(colors) // 차트 요소별 색상 설정
            valueTextColor = Color.BLACK // 값(백분율) 표시 색상 설정
            valueTextSize = 30f // 값 크기
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
            holeRadius = 0f // 가운데 빈 구멍 크기
            transparentCircleRadius = 0f // 투명한 부분 크기
            centerText = null // 가운데 텍스트 없앰
            setEntryLabelTextSize(20f) // label 글씨 크기
            setEntryLabelColor(Color.BLACK) // label 색상
            animateY(800, Easing.EaseInOutQuad) // 0.8초 동안 애니메이션 설정
            animate()
        }
    }
}

// primary 생성자 - 액티비티 내 텍스트뷰 객체를 전달(3라인 각각 다)
class EnergyReview(
    private val totalCalories: TextView,
    private val review1: TextView,
    private val review2: TextView) {

    // =========================================================================
    // set 메소드
    // =========================================================================

    fun showErrorMsg() {
        totalCalories.text = "섭취량 기록을 불러오는데 실패했습니다.\n인터넷 연결 상태를 다시 확인해주세요."
        review1.visibility = View.GONE
        review2.visibility = View.GONE

    }

    fun showNoDataMsg(context: Context) {
        totalCalories.text = "아직 섭취량 기록이 없어요."
        review1.text = "섭취량 기록을 남겨보세요!"
        review2.text = "기록을 남기면 영양 관리를 도울 \n분석 결과를 제공해드려요!"
        review2.setTextColor(ContextCompat.getColor(context, R.color.highlight))
    }

    // 앱 전체에서 쓰는 색상 정의 xml 활용하기 위해 context 전달 필요.
    // 액티비티 내에서 쓸 때 this로 context 전달하면 됨. ex) setTextViews(this, ...)
    fun setTextViews(context: Context, energyIntake:Int, energyDV:Int?){

        val calrText1 = "총 "
        val calrText2 = "${energyIntake}kcal"
        val calrText3 = "를 섭취했습니다!"
        val totalCalrText = calrText1 + calrText2 + calrText3

        // spannable 타입으로 문자열 일부별 다른 텍스트 속성 적용(색상, 스타일)
        val builder = SpannableStringBuilder(totalCalrText)

        // 총 칼로리 부분 스타일 지정(제목3)
        val styleSigSpan = TextAppearanceSpan(context, R.style.title3)
        builder.setSpan(
            styleSigSpan,
            calrText1.length,
            calrText1.length + calrText2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        energyDV?.let {energyDV ->  // 일일 권장량 기준 있을 때만 전용 문구 및 색상 구분 설정
            var sigColor = 0 // 총 칼로리 및 섭취량 평가 포인트 색상
            when(energyIntake) {
                in 0..< energyDV -> { // 적정 범위 미만
                    sigColor = ContextCompat.getColor(context, R.color.yellow)
                    // 총 칼로리 섭취량 평가
                    review1.text = "오늘의 필요 에너지량보다\n" + "${energyDV-energyIntake}kcal 적습니다"
                    review2.text = "더 든든하게 식사하세요!"
                }
                in energyDV..energyDV+200 -> { // 적정 범위
                    sigColor = ContextCompat.getColor(context, R.color.green)
                    // 총 칼로리 섭취량 평가
                    review1.text = "오늘의 필요 에너지량을 충족했어요"
                    review2.text = "정말 잘 하셨어요!\uD83C\uDF89"
                }
                else -> { // 적정 범위 초과
                    sigColor = ContextCompat.getColor(context, R.color.highlight)
                    // 총 칼로리 섭취량 평가
                    review1.text = "오늘의 필요 에너지량을 초과했어요\n" + "체중 조절에 주의하세요!"
                    review2.text = "평소보다 움직임을 늘려보는 게 어떨까요?"
                }
            }
            // 포인트 색상 적용(노랑/초록/빨강)
            val colorSigSpan = ForegroundColorSpan(sigColor)
            builder.setSpan(
                colorSigSpan,
                calrText1.length,
                calrText1.length + calrText2.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            review1.setTextColor(sigColor)
        }

        // spannable 텍스트 적용
        totalCalories.text = builder
    }

}

// primary 생성자 - 액티비티 내 바 차트 객체 및 관련 텍스트뷰를 전달
class NutriIntakeBarDisplay(
    val barChart : BarChart,
    val dvTextView : TextView,
    val iconTextView : TextView) {

    fun setBarValue(context: Context, nutri: Nutrition, dv: DailyValue) {
        val milliIntake = nutri.getMilliGram()
        val percentIntake = nutri.getPercentageOfDailyValue(dv.dailyValue)

        // 권장 섭취량 및 실제 섭취량에 따른 표시 설정(텍스트, 색상)
        val intakeStatus = dv.getIntakeStatus(milliIntake)

        intakeStatus?.let { inStat ->
            // 섭취량 상태에 따른 표시 색상
            val colorInt = ContextCompat.getColor(context, inStat.status.colorRID)

            // 맞춤 권장 섭취량 표시
            dvTextView.text = "나의 권장량: " + dv.getDVString()

            // 섭취량 상태에 따른 우측 아이콘 표시
            when(inStat.status) {
                Status.WARNING -> {
                    iconTextView.text="⚠"
                    iconTextView.setTextColor(colorInt)
                    dvTextView.setTextColor(colorInt)
                }
                Status.CAUTION -> {
                    iconTextView.text=""
                }
                Status.SATISFIED -> {
                    iconTextView.text=""
                }
            }

            // 바 표시 설정
            val entries = arrayListOf<BarEntry>()
            entries.add(BarEntry(0f, percentIntake.toFloat()))
            applyBarChart(context, entries, colorInt, 100f)
        }

    }

    fun hide(){
        barChart.visibility = View.GONE
        dvTextView.visibility = View.GONE
        iconTextView.visibility = View.GONE
    }

    fun setZero(context: Context, unit: UnitOfMass, dv: DailyValue) {
        setBarValue(context, Nutrition(0, unit), dv)
    }

    // 바 표시 설정
    fun applyBarChart(context: Context, entries: List<BarEntry>, color: Int, maximum: Float) {

        barChart.description.isEnabled = false // chart 밑에 description 표시 유무
        barChart.setTouchEnabled(false) // 터치 유무
        barChart.legend.isEnabled = false // 레전드 삭제
        barChart.setDrawBarShadow(true) // 회색 배경(그림자 효과)

        // 모서리 둥근 바 차트
        val renderer = RoundedHorizontalBarChartRenderer(barChart, barChart.animator, barChart.viewPortHandler)
        renderer.setRightRadius(60f)
        barChart.renderer = renderer

        // XAxis (수평 막대 기준 왼쪽)
        barChart.xAxis.isEnabled = false

        // YAxis(Left) (수평 막대 기준 위쪽)
        val axisLeft: YAxis = barChart.getAxisLeft()
        axisLeft.axisMinimum = 0f // 최솟값
        axisLeft.axisMaximum = maximum // 최댓값
        axisLeft.isEnabled = false

        // YAxis(Right) (수평 막대 기준 아래쪽)
        barChart.axisRight.isEnabled = false

        // 차트 밖이 아니라 내부에 값 표시
        barChart.setDrawValueAboveBar(false)

        // 2. [BarDataSet] 단순 데이터를 막대 모양으로 표시, BarChart의 막대 커스텀
        val set = BarDataSet(entries, "Nutri Intake")
        set.setDrawIcons(false)
        set.setDrawValues(true)
        set.color = color // 색상 설정
        set.barShadowColor = ContextCompat.getColor(context, R.color.chartgrey)

        //set.valueFormatter = PercentFormatter() // 값 표시 String 포맷 설정

        // 커스텀 퍼센트 포맷터 -> 비율을 float이 아니라 int형으로 표시
        set.valueFormatter = object : ValueFormatter() { // 값 표시 String 포맷 설정
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString() + "%"
            }
        }

        // 3. [BarData] 보여질 데이터 구성
        val data = BarData(set)
        data.barWidth = 1f
        data.setValueTextColor(ContextCompat.getColor(context, R.color.white))
        data.setValueTextSize(18f)
        data.setValueTypeface(ResourcesCompat.getFont(context, R.font.pretendard700))

        // 모든 차트 및 데이터 설정 적용
        barChart.animateY(300)
        barChart.setData(data)

        barChart.invalidate()

    }
}

class AllIntakeBarDisplay(
    val natriumBar: NutriIntakeBarDisplay,
    val carbsBar: NutriIntakeBarDisplay,
    val sugarBar: NutriIntakeBarDisplay,
    val proteinBar: NutriIntakeBarDisplay,
    val fatBar: NutriIntakeBarDisplay,
    val satFatBar: NutriIntakeBarDisplay,
    val cholBar: NutriIntakeBarDisplay,
    val lackIntakeReview : TextView,
    val overIntakeReview : TextView
    ) {

    fun hide(context: Context, userDVs: NutrientDailyValues?) {

        userDVs?.let { // 사용자 맞춤 권장량 O
            setNoDataValues(context, it)
        }?:{ // 사용자 맞춤 권장량 객체 null 일때 바 차트 다 숨김
            natriumBar.hide()
            carbsBar.hide()
            sugarBar.hide()
            proteinBar.hide()
            fatBar.hide()
            satFatBar.hide()
            cholBar.hide()
        }

        lackIntakeReview.visibility = View.GONE
        overIntakeReview.visibility = View.GONE
    }

    fun setNoDataValues(context: Context, userDVs: NutrientDailyValues) {
        natriumBar.setZero(context, UnitOfMass.MILLIGRAM, userDVs.natrium)
        carbsBar.setZero(context, UnitOfMass.GRAM, userDVs.carbs)
        sugarBar.setZero(context, UnitOfMass.GRAM, userDVs.sugar)
        proteinBar.setZero(context, UnitOfMass.GRAM, userDVs.protein)
        fatBar.setZero(context, UnitOfMass.GRAM, userDVs.fat)
        satFatBar.setZero(context, UnitOfMass.GRAM, userDVs.satFat)
        cholBar.setZero(context, UnitOfMass.MILLIGRAM, userDVs.chol)
    }

    fun setReviews(context: Context, intake:NutritionFacts, userDVs: NutrientDailyValues){
        var overNutris = arrayListOf<String>()
        var lackNutris = arrayListOf<String>()

        intake.natrium?.let {
            userDVs.natrium.getIntakeStatus(it.getMilliGram())?.let { range ->
                when(range){
                    IntakeRange.LACK -> lackNutris.add("나트륨")
                    IntakeRange.OVER -> overNutris.add("나트륨")
                    else -> {}
                }
            }
        }
        intake.carbs?.let {
            userDVs.carbs.getIntakeStatus(it.getMilliGram())?.let { range ->
                when(range){
                    IntakeRange.LACK -> lackNutris.add("탄수화물")
                    IntakeRange.OVER -> overNutris.add("탄수화물")
                    else -> {}
                }
            }
        }
        intake.sugar?.let {
            userDVs.sugar.getIntakeStatus(it.getMilliGram())?.let { range ->
                when(range){
                    IntakeRange.LACK -> lackNutris.add("당")
                    IntakeRange.OVER -> overNutris.add("당")
                    else -> {}
                }
            }
        }
        intake.protein?.let {
            userDVs.protein.getIntakeStatus(it.getMilliGram())?.let { range ->
                when(range){
                    IntakeRange.LACK -> lackNutris.add("단백질")
                    IntakeRange.OVER -> overNutris.add("단백질")
                    else -> {}
                }
            }
        }
        intake.fat?.let {
            userDVs.fat.getIntakeStatus(it.getMilliGram())?.let { range ->
                when(range){
                    IntakeRange.LACK -> lackNutris.add("지방")
                    IntakeRange.OVER -> overNutris.add("지방")
                    else -> {}
                }
            }
        }
        intake.satFat?.let {
            userDVs.satFat.getIntakeStatus(it.getMilliGram())?.let { range ->
                when(range){
                    IntakeRange.LACK -> lackNutris.add("포화지방")
                    IntakeRange.OVER -> overNutris.add("포화지방")
                    else -> {}
                }
            }
        }
        intake.chol?.let {
            userDVs.chol.getIntakeStatus(it.getMilliGram())?.let { range ->
                when(range){
                    IntakeRange.LACK -> lackNutris.add("콜레스테롤")
                    IntakeRange.OVER -> overNutris.add("콜레스테롤")
                    else -> {}
                }
            }
        }

        if ( lackNutris.size > 0) {

            val lackNutrisText = lackNutris.joinToString()
            val lackWarning = " 섭취가 부족해요"
            val warningText = lackNutrisText + lackWarning

            // spannable 타입으로 문자열 일부별 다른 텍스트 속성 적용(색상, 스타일)
            val builder = SpannableStringBuilder(warningText)

            // 색상 지정
            val warningColor = ContextCompat.getColor(context, R.color.highlight)

            builder.setSpan(
                ForegroundColorSpan(warningColor),
                0,
                lackNutrisText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // spannable 텍스트 적용
            lackIntakeReview.text = builder

        } else {
            lackIntakeReview.visibility = View.GONE
        }

        if ( overNutris.size > 0) {

            val overNutrisText = overNutris.joinToString()
            val overWarning = " 섭취를 지금보다 줄이는 게 좋아요!"
            val warningText = overNutrisText + overWarning

            // spannable 타입으로 문자열 일부별 다른 텍스트 속성 적용(색상, 스타일)
            val builder = SpannableStringBuilder(warningText)

            // 색상 지정
            val warningColor = ContextCompat.getColor(context, R.color.highlight)

            builder.setSpan(
                ForegroundColorSpan(warningColor),
                0,
                overNutrisText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // spannable 텍스트 적용
            overIntakeReview.text = builder

        } else {
            overIntakeReview.visibility = View.GONE
        }
    }

    fun setAll(context: Context, totalIntake:NutritionFacts, userDVs: NutrientDailyValues?) {
        userDVs?.let { userDVs ->
            totalIntake.natrium?.let {
                natriumBar.setBarValue(context, it, userDVs.natrium)
            } ?: run {
                natriumBar.setBarValue(context, Nutrition(0, UnitOfMass.MILLIGRAM), userDVs.natrium)
            }

            totalIntake.carbs?.let {
                carbsBar.setBarValue(context, it, userDVs.carbs)
            } ?: run {
                carbsBar.setBarValue(context, Nutrition(0, UnitOfMass.GRAM), userDVs.carbs)
            }

            totalIntake.sugar?.let {
                sugarBar.setBarValue(context, it, userDVs.sugar)
            } ?: run {
                sugarBar.setBarValue(context, Nutrition(0, UnitOfMass.GRAM), userDVs.sugar)
            }

            totalIntake.protein?.let {
                proteinBar.setBarValue(context, it, userDVs.protein)
            } ?: run {
                proteinBar.setBarValue(context, Nutrition(0, UnitOfMass.GRAM), userDVs.protein)
            }

            totalIntake.fat?.let {
                fatBar.setBarValue(context, it, userDVs.fat)
            } ?: run {
                fatBar.setBarValue(context, Nutrition(0, UnitOfMass.GRAM), userDVs.fat)
            }

            totalIntake.satFat?.let {
                satFatBar.setBarValue(context, it, userDVs.satFat)
            } ?: run {
                satFatBar.setBarValue(context, Nutrition(0, UnitOfMass.GRAM), userDVs.satFat)
            }

            totalIntake.chol?.let {
                cholBar.setBarValue(context, it, userDVs.chol)
            } ?: run {
                cholBar.setBarValue(context, Nutrition(0, UnitOfMass.MILLIGRAM), userDVs.chol)
            }

            setReviews(context, totalIntake, userDVs)
        }?:{
            // 사용자 맞춤 권장량 객체 null 일때 바 차트 다 숨김
            natriumBar.hide()
            carbsBar.hide()
            sugarBar.hide()
            proteinBar.hide()
            fatBar.hide()
            satFatBar.hide()
            cholBar.hide()
        }
    }

}

class PercentOfDailyValueLineView(val labelTextView : TextView, val percentTextView: TextView){

    fun set(nutriLabel:String, percentValue: Int) {
        if (isSubNutri(nutriLabel))
            labelTextView.text = "ㄴ" + nutriLabel
        else
            labelTextView.text = nutriLabel

        percentTextView.text = "${percentValue}%"
    }

    fun disable(nutriLabel:String) {
        if (isSubNutri(nutriLabel))
            labelTextView.text = "ㄴ" + nutriLabel
        else
            labelTextView.text = nutriLabel
        percentTextView.text = "제공 불가"
    }

    @SuppressLint("ResourceAsColor")
    fun highlight(context: Context) {
        labelTextView.setTextColor(ContextCompat.getColor(context, R.color.highlight))
        percentTextView.setTextColor(ContextCompat.getColor(context, R.color.highlight))
    }

    fun isSubNutri(label:String) : Boolean{
        return label == "포화지방" || label == "당류"
    }
}

class PercentViewOfNutritionFacts(
    val cautionTextView : TextView,
    val line0 : TextView,
    val viewLines : ArrayList<PercentOfDailyValueLineView>) {

    fun disable(){
        val allNutris =
            arrayOf("나트륨", "탄수화물", "당류", "지방", "포화지방", "콜레스테롤", "단백질")
        cautionTextView.visibility = View.GONE
        for(i in 0 until viewLines.size) { // 그외 영양소들
            val label = allNutris[i]
            viewLines[i].disable(label)
        }
    }

    fun setWarningText(diseaseList: MutableSet<String>) {
        cautionTextView.text = "⚠ " + diseaseList.joinToString() + "에 주의해야 할 성분을 포함해요"
    }

    fun hideWarningText() {
        cautionTextView.visibility = View.GONE
    }

    fun setLineViews(context: Context, nutriFacts: NutritionFacts, userDVs: NutrientDailyValues?, nutrisToCare: Array<String>) {
        val allNutris =
            arrayOf("나트륨", "탄수화물", "당류", "지방", "포화지방", "콜레스테롤", "단백질")
        val others = allNutris.subtract(nutrisToCare.toSet()).toTypedArray()

        line0.setTextColor(ContextCompat.getColor(context, R.color.highlight))
        for(i in 0 until nutrisToCare.size){ // 질환에 주의해야할 영양소들
            val label = nutrisToCare[i]
            userDVs?.let {
                val percent = nutriFacts.getPercentOfDailyValueByNutriLabel(label, it)
                viewLines[i].set(label, percent)
            }?:{
                viewLines[i].disable(label)
            }
            viewLines[i].highlight(context)
        }
        for(i in nutrisToCare.size until viewLines.size) { // 그외 영양소들
            val label = others[i-nutrisToCare.size]
            userDVs?.let {
                val percent = nutriFacts.getPercentOfDailyValueByNutriLabel(label, it)
                viewLines[i].set(label, percent)
            }?:{
                viewLines[i].disable(label)
            }
        }
    }

    fun setLineViews(nutriFacts: NutritionFacts, userDVs: NutrientDailyValues?) {
        val allNutris =
            arrayOf("나트륨", "탄수화물", "당류", "지방", "포화지방", "콜레스테롤", "단백질")
        for(i in 0 until viewLines.size) { // 그외 영양소들
            val label = allNutris[i]
            userDVs?.let {
                val percent = nutriFacts.getPercentOfDailyValueByNutriLabel(label, it)
                viewLines[i].set(label, percent)
            }?:{
                viewLines[i].disable(label)
            }
        }
    }
}

class AllergyChipView(
    val chipGroup: ChipGroup, val textView: TextView? = null) {

    fun set(
        context: Context, foodAllergy:Array<String>, userAllergy:Array<String>, ) {

        val allergyWarningList = userAllergy.intersect(foodAllergy.toSet())

        textView?.let { textView ->
            if (allergyWarningList.isNotEmpty()){
                textView.text = "⚠ 내가 주의할 알레르기 성분이 있어요"
                textView.setTextColor(ContextCompat.getColor(context, R.color.highlight))
                for (item in allergyWarningList) {
                    val chip = Chip(context)
                    chip.text = item

                    // Chip 뷰의 크기 및 여백 설정
                    val params = ChipGroup.LayoutParams(
                        250, // 넓이 80
                        160  // 높이 50
                    )
                    params.setMargins(8, 8, 8, 8) // 여백을 8로..
                    chip.layoutParams = params

                    // 글씨 크기
                    chip.textSize = 25f
                    chip.setTextAppearance(context, R.style.title3)
                    chip.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.highlight)
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.white)
                    chip.chipStrokeWidth = 7f

                    // 가운데 정렬
                    chip.textAlignment = View.TEXT_ALIGNMENT_CENTER

                    chipGroup.addView(chip)
                }
            }
        }
    }

    fun set(context: Context, foodAllergy:Array<String>) {

        for (item in foodAllergy) {
            val chip = Chip(context)
            chip.text = item

            // Chip 뷰의 크기 및 여백 설정
            val params = ChipGroup.LayoutParams(
                250, // 넓이 80
                150  // 높이 50
            )
            params.setMargins(8, 8, 8, 8) // 여백을 8로..
            chip.layoutParams = params

            // 글씨 크기
            chip.textSize = 25f
            chip.setTextAppearance(context, R.style.title3)

            // 가운데 정렬
            chip.textAlignment = View.TEXT_ALIGNMENT_CENTER

            chipGroup.addView(chip)
        }
    }

}


