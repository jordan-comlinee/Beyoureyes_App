package com.example.beyoureyes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter


class TodayIntakePersonalizedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_intake_personalized)
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)

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


        // HorizontalBarChart 초기화
        val carboBarChart = findViewById<HorizontalBarChart>(R.id.carboBarchart)
        val fatBarChart = findViewById<HorizontalBarChart>(R.id.fatBarchart)
        val proteinBarChart = findViewById<HorizontalBarChart>(R.id.proBarchart)
        val naBarChart = findViewById<HorizontalBarChart>(R.id.naBarchart)
        val choleBarChart = findViewById<HorizontalBarChart>(R.id.choleBarchart)
        val satFatBarChart = findViewById<BarChart>(R.id.satFatBarchart)

        // 바 차트의 데이터 설정
        val entries = arrayListOf<BarEntry>()
        entries.add(BarEntry(48f, 72f))

        applyBarChart(carboBarChart, entries,"#FF0000", 80f)
        applyBarChart(fatBarChart, entries, "#F1BC00", 100f)
        applyBarChart(proteinBarChart, entries, "#34CA00", 120f)
        applyBarChart(naBarChart, entries, "#34CA00", 100f)
        applyBarChart(choleBarChart, entries, "#34CA00", 130f)
        applyBarChart(satFatBarChart, entries, "#FF0000", 72f)
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
        xAxis.setEnabled(true)
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
    }

}