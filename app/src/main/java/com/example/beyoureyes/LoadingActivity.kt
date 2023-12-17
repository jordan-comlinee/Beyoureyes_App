package com.example.beyoureyes

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.io.File


class LoadingActivity : AppCompatActivity() {
    private val handler = Handler()

    private val extractedWords = mutableSetOf<String>()


    private lateinit var textView: TextView // 텍스트 뷰 선언

    private lateinit var btn: Button


    private val textRecognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    ) // 한글 텍스트 인식 인스턴스 생성
    private val pickImage = 100 // 이미지 선택 요청 코드
    private val koreanCharactersSet = mutableSetOf<String>() // 한글 문자를 담을 Set, 중복 피하기 위해 Set 자료구조 활용
    private val gList = mutableListOf<String>() // "숫자" + "g/mg" 를 담을 List
    private val percentList = mutableListOf<String>() // "숫자" + "%"를 담을 List
    private val kcalList = mutableListOf<String>() // "kcal"와 "g 당"을 담을 List
    private val keywords = listOf("나트", "탄수", "지방", "당류", "트랜스", "포화", "콜레", "단백") // 특정 키워드 List

    private lateinit var moPercentList: List<String> // % -> g 으로 변형하여 담을 List


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        textView = findViewById(R.id.textView) // 텍스트뷰 초기화

        btn = findViewById(R.id.btn) // 버튼 초기화


        val filePath = intent.getStringExtra("bitmapPath")

        if (filePath != null) {
            val f = File(filePath)
            val bitmap = BitmapFactory.decodeFile(filePath)
            if (bitmap != null) {
                detectTextInBitmap(bitmap) // 필터링 알고리즘
            }
            f.delete()
        }


        // 필터링 변형한 데이터 intent 전달 알고리즘
        btn.setOnClickListener {
            val isValidData = isValidData()
            val isValidAlergyData = isValidData_alergy()
            val hasValidKeywordOrder = checkKeywordOrder(koreanCharactersSet)
            val isValidPercentData = isValidData_per()

            when {
                isValidData && isValidAlergyData && hasValidKeywordOrder && isValidPercentData -> { // 알레르기 & 영양성분
                    startFoodInfoAllActivity()
                }
                isValidData && hasValidKeywordOrder && isValidPercentData -> { // 영양성분
                    startFoodInfoNutritionActivity()
                }
                isValidAlergyData -> { // 알레르기
                    startFoodInfoAllergyActivity()
                }
                else -> {
                    showAlertDialog("Camera Activity로 이동") // 인식 실패 시 Alert 창
                }
            }
        }


        handler.postDelayed({
            btn.performClick() // 버튼을 자동으로 클릭
        }, 5000) // 5초

    }
    private fun startFoodInfoAllActivity() {
        val intent = Intent(this, FoodInfoAllActivity::class.java)
        configureIntent(intent)
        intent.putStringArrayListExtra("allergyList", ArrayList(extractedWords.toList()))
        startActivity(intent)
    }

    private fun startFoodInfoNutritionActivity() {
        val intent = Intent(this, FoodInfoNutritionActivity::class.java)
        configureIntent(intent)
        startActivity(intent)
    }

    private fun startFoodInfoAllergyActivity() {
        val intent = Intent(this, FoodInfoAllergyActivity::class.java)
        intent.putStringArrayListExtra("allergyList", ArrayList(extractedWords.toList()))
        startActivity(intent)
    }

    private fun configureIntent(intent: Intent) {
        intent.putExtra("modifiedPercentList", ArrayList(moPercentList))
        intent.putExtra("PercentList", ArrayList(percentList))
        intent.putStringArrayListExtra("modifiedKcalListText", ArrayList(kcalList))
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // 핸들러 제거
    }

    // 한글 키워드의 순서 확인하는 함수
    private fun checkKeywordOrder(keywordSet: Set<String>): Boolean {
        val targetKeywords = listOf("나트", "탄수화", "지방", "당류", "트랜스", "포화지방", "콜레스", "단백질")
        val keywordList = keywordSet.toList()

        var targetIndex = 0
        for (keyword in keywordList) {
            val partialMatch = targetKeywords.any { keyword.contains(it) }
            if (partialMatch) {
                targetIndex++
            }

            if (targetIndex == targetKeywords.size) {
                // 모든 키워드가 순서대로 나타났으면 true 반환
                return true
            }
        }

        // 여기까지 왔다면 순서가 일치하지 않음
        return false
    }

    private fun isValidData(): Boolean { // 퍼센트와 칼로리 유효성 판단

        return percentList.isNotEmpty() && kcalList.isNotEmpty() && percentList.size == 7
    }

    private fun isValidData_alergy(): Boolean { // 알레르기 유효성 판단

        return extractedWords.isNotEmpty()
    }

    private fun isValidData_per(): Boolean { // 퍼센트가 100% 이상일 때 false 반환 -> OCR 인식 오류 방지 위해
        for (percent in percentList) {
            if (percent.toInt() >= 100) {
                return false
            }
        }
        return true
    }

    // 알림 다이얼로그 표시 함수
    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("알림")
            .setMessage(message)
            .setPositiveButton("확인") { _, _ -> }
            .show()
    }

    // 제외할 단어를 고려한 텍스트 인식 함수
    private fun detectTextInBitmap(bitmap: Bitmap) {
        try {
            // 새 이미지를 처리하기 전에 세트와 리스트를 초기화!
            koreanCharactersSet.clear()
            gList.clear()
            percentList.clear()

            val image = InputImage.fromBitmap(bitmap, 0)
            textRecognizer.process(image)
                .addOnSuccessListener { result ->
                    // 알레르기 필터링 알고리즘 병합
                    val targetWords = listOf(
                        "메밀", "밀", "대두", "땅콩", "호두", "잣", "계란",
                        "아황산류", "복숭아", "토마토", "난류", "우유", "새우",
                        "고등어", "오징어", "게", "조개류", "돼지고기", "쇠고기", "닭고기"
                    )


                    var foundHamuIndex: Int? = null // 함유 인덱스 파악 위해

                    val lines = result.text.split('\n')

                    for ((index, lineText) in lines.withIndex()) {
                        if (lineText.contains("함유")) {
                            foundHamuIndex = index

                            val previousLineIndex = index - 1
                            if (previousLineIndex >= 0) {
                                val previousLineText = lines[previousLineIndex]
                                for (targetWord in targetWords) {
                                    if (previousLineText.contains(targetWord)) {
                                        extractedWords.add(targetWord)
                                    }
                                }
                            }
                        }

                        if (foundHamuIndex != null && foundHamuIndex == index) {
                            for (targetWord in targetWords) {
                                if (lineText.contains(targetWord)) {
                                    extractedWords.add(targetWord)
                                }
                            }
                        }

                    }
                    val allRecognizedWords = result.text
                    val extractedText = extractedWords.joinToString(", ")
                    runOnUiThread { // OCR 결과 학인 위해.. 나중에 제거할 예정
                        textView.append("감지된 텍스트: $extractedText\n")
                        // textView.append("모든 인식된 텍스트: $allRecognizedWords\n")
                    }
                    for (block in result.textBlocks) {
                        for (line in block.lines) {
                            for (element in line.elements) {
                                val elementText = element.text
                                //특정 키워드 확인, 한글 Set는 요소별로 추출
                                for (keyword in keywords) {
                                    if (elementText.contains(keyword)) {
                                        // 키워드를 한글 문자 Set에 추가
                                        koreanCharactersSet.add(elementText)
                                        // replaceKoreanCharacters(koreanCharactersSet)
                                    }
                                }
                            }
                            val lineText = line.text
                            val lineInfo = "라인 텍스트: $lineText" // 전체 인식한 라인 텍스트 출력
                            runOnUiThread { // OCR 결과 학인 위해.. 나중에 제거할 예정
                                textView.append("$lineInfo\n")
                            }

                            // "숫자 g" 형태 확인
                            extractNumberG(lineText)

                            // "%" 형태 확인
                            extractPercent(lineText)

                            // "숫자 kcal, 숫자 g당" 형태 확인
                            extractNumberKcal(lineText)

                            // "숫자 g)" 형태 확인
                            extractNumberGBracket(lineText)
                        }

                    }
                    // 이미지 처리 후에 결과를 출력
                    showResults()

                }
                .addOnFailureListener { e ->

                    e.printStackTrace()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //"숫자 g" 형태를 추출하는 함수 - g, mg, 9 앞에 숫자를 추출함
    private fun extractNumberG(lineText: String) {
        val regex = """(\d+(\.\d+)?)\s*(g|mg|9)(?!\s*당)""".toRegex() // "숫자" + "당"은 제외
        val matchResults = regex.findAll(lineText)
        for (matchResult in matchResults) {
            val (number) = matchResult.destructured

            val openingIndex = lineText.indexOf('(') // "(" + "숫자" 는 제외
            if (openingIndex == -1 || openingIndex > matchResult.range.first) {
                gList.add(number)
            }
        }
    }

    // "%" 형태를 추출하는 함수
    private fun extractPercent(lineText: String) {
        val percentRegex = """(\d+(\.\d+)?)\s*%""".toRegex()
        val percentMatchResults = percentRegex.findAll(lineText)
        for (percentMatchResult in percentMatchResults) {
            val (percentNumber) = percentMatchResult.destructured
            percentList.add(percentNumber)
        }
    }

    // "숫자 kcal" 형태를 추출하는 함수
    private fun extractNumberKcal(lineText: String) {
        val kcalRegex = """(\d+(\.\d+)?)\s*(kcal)""".toRegex()
        val kcalMatchResults = kcalRegex.findAll(lineText)
        for (kcalMatchResult in kcalMatchResults) {
            val (kcalNumber) = kcalMatchResult.destructured
            kcalList.add(kcalNumber + "kcal")
        }

    }


    // "g 당" 및 "총 내용량 + 숫자" 형태를 추출하는 함수
    private fun extractNumberGBracket(lineText: String) {
        val gPattern = """(\d+(\.\d+)?)\s*(g)\)""".toRegex() // "숫자" + "g)"
        val gDangPattern = """(\d+(\.\d+)?)\s*(g|mg|9)\s*당""".toRegex() // g/mg/9 + "당"

        val gMatches = gPattern.findAll(lineText)
        for (matchResult in gMatches) {
            val (number) = matchResult.destructured
            kcalList.add(number)
        }

        val gDangMatches = gDangPattern.findAll(lineText)
        for (matchResult in gDangMatches) {
            val (number) = matchResult.destructured
            kcalList.add(number)
        }
    }


    // 칼로리 필터링 및 변형 함수, 함수 이름 수정 예정
    private fun showResults() {

        // "kcal" 리스트에서 "000kcal" 및 "2000kcal" 제거
        kcalList.removeAll(listOf("000kcal", "2.000kcal", "2000kcal", "2,000kcal"))

        // kcalList에 항목이 하나만 있는지 확인, 계산 필요하지 확인 위해
        if (kcalList.size == 1) { // 항목이 하나면 계산이 필요하지 않음. "숫자" + "kcal"에서 "kcal"를 제거
            val kcalValue = kcalList[0].replace("[^\\d.]".toRegex(), "")
            kcalList.clear()
            kcalList.add(kcalValue)
        } else if (kcalList.size == 2) { // 항목이 두개면 총 kcal 계산 필요
            // 정규 표현식을 사용하여 "kcal" 부분을 제거
            val firstKcal = kcalList[0].replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val secondKcal = kcalList[1].replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 1.0 // 기본값은 1.0으로 설정

            if (secondKcal != 0.0) {
                val result = firstKcal / secondKcal // 1g당 칼로리 계산

                // gList의 첫번째 인덱스 값 가져옴
                val gListValue = if (gList.isNotEmpty()) gList[0].toDoubleOrNull() ?: 1.0 else 1.0

                // 총 kcal 값 계산
                val resultWithoutUnit = (result * gListValue).toInt().toString()

                kcalList.clear() // kcalList 비우고 계산된 결과 담기
                kcalList.add(resultWithoutUnit)
            }else {
                // 두 번째 값이 0이면 나눌 수 없음을 나타내는 메시지 출력
                //println("0이라 계산할 수 없습니다.")
            }
        }

        // g계산 알고리즘 함수 결과
        moPercentList = modiPercentList(percentList)

    }

}


// % 를 이용하여 g을 계산
private fun modiPercentList(percentList: List<String>): List<String> {
    if (percentList.size != 7) {
        // 퍼센트 리스트의 길이가 7이 아니면 빈 리스트를 반환
        return emptyList()
    }

    val modifiedList = percentList.mapIndexed { index, percent ->
        val modifiedPercent = when (index) {
            0 -> ((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 2000).toInt().toString()
            1 -> ((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 324).toInt().toString()
            2 -> ((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 100).toInt().toString()
            3 -> ((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 54).toInt().toString()
            4 -> ((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 15).toInt().toString()
            5 -> ((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 300).toInt().toString()
            6 -> ((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 55).toInt().toString()

            else -> ((percent.toDoubleOrNull() ?: 0.0) * 0.01 * 2000).toInt().toString() // 그냥 한 값
        }
        modifiedPercent
    }

    return modifiedList
}






