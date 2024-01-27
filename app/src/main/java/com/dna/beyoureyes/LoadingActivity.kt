package com.dna.beyoureyes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dna.beyoureyes.databinding.ActivityLoadingBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.io.File


class LoadingActivity : AppCompatActivity() {
    private val handler = Handler()

    private val extractedWords = mutableSetOf<String>()

    private lateinit var resultbtn: Button


    private val textRecognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    ) // 한글 텍스트 인식 인스턴스 생성
    private val pickImage = 100 // 이미지 선택 요청 코드
    private val koreanCharactersList = mutableListOf<String>() // 한글 문자를 담을 Set, 중복 피하기 위해 Set 자료구조 활용
    private var koreanCharactersListmodi = mutableListOf<String>()
    private val gList = mutableListOf<String>() // "숫자" + "g/mg" 를 담을 List
    private val percentList = mutableListOf<String>() // "숫자" + "%"를 담을 List
    private val kcalList = mutableListOf<String>() // "kcal"와 "g 당"을 담을 List
    private val keywords = listOf("나트", "탄수", "지방", "당류", "트랜스", "포화", "콜레", "단백") // 특정 키워드 List

    private lateinit var moPercentList: List<String> // % -> g 으로 변형하여 담을 List

    private lateinit var binding: ActivityLoadingBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        textView = findViewById(R.id.textView) // test 하기 위해.. 삭제예정

        resultbtn = binding.resultbtn


        val filePath = intent.getStringExtra("bitmapPath")

        if (filePath != null) {
            Log.d("YourTag", "File path: $filePath") // 파일 경로 로깅

            val f = File(filePath)
            val bitmap = BitmapFactory.decodeFile(filePath)
            if (bitmap != null) {
                detectTextInBitmap(bitmap) // 필터링 알고리즘
            } else {
                Log.e("bitmap", "Bitmap is null") // 비트맵이 null일 때
            }
            f.delete()
        } else {
            Log.e("bitmap", "File path is null") // 파일 경로가 null일 때
        }


        resultbtn.setOnClickListener {

//            textView.append(moPercentList.toString())

            val isValidData = isValidData()
            val isValidAllergyData = isValidData_alergy()
            val hasValidKeywordOrder = checkKeywordOrder(koreanCharactersListmodi)
            val isValidPercentData = isValidData_per()

            when {
                hasValidKeywordOrder && isValidData &&  isValidPercentData && isValidAllergyData  -> {
                    startFoodInfoAllActivity()
                }
                hasValidKeywordOrder && isValidData && isValidPercentData -> {
                    startFoodInfoNutritionActivity()
                }
                isValidAllergyData -> {
                    startFoodInfoAllergyActivity()
                }
                else -> {
                    val intent = Intent(this, CameraOcrproblemActivity::class.java)
                    startActivity(intent)
                }
            }
        }


        handler.postDelayed({
            resultbtn.performClick() // 버튼을 자동으로 클릭
        }, 4000) // 4초

    }

    private fun useTestInfo() {
        // 추출 키워드 값 임의 설정
        koreanCharactersList.clear()
        koreanCharactersList.add("나트륨")
        koreanCharactersList.add("탄수화물")
        koreanCharactersList.add("당류")
        koreanCharactersList.add("지방")
        koreanCharactersList.add("트랜스지방")
        koreanCharactersList.add("포화지방")
        koreanCharactersList.add("콜레스테롤")
        koreanCharactersList.add("단백질")



        koreanCharactersListmodi = koreanCharactersList.distinct().toMutableList()
        koreanCharactersListmodi = koreanCharactersListmodi.map {
            it.replace(Regex("[^가-힣]"), "")
        }.toMutableList()


        // % 리스트 값 임의 설정
        percentList.clear()
        percentList.add("17") // 나트륨%
        percentList.add("17") // 탄수화물%
        percentList.add("9") // 당류%
        percentList.add("20") // 지방%
        percentList.add("19") // 포화지방%
        percentList.add("5") // 콜레스테롤%
        percentList.add("13") // 단백질%

        // % -> g 리스트 값 설정
        moPercentList = modiPercentList(percentList)
        if (moPercentList.size == 0) {
            Log.d("test", "moPercentList is empty")
        }


        // kcal 리스트 값 설정
        kcalList.clear()
        kcalList.add("343")


        // 알레르기 값 설정
        extractedWords.clear()
        extractedWords.add("밀")
        extractedWords.add("땅콩")
        extractedWords.add("새우")
    }

    private fun startFoodInfoAllActivity() {
        val intent = Intent(this, FoodInfoAllActivity::class.java)
        intent.putExtra("modifiedPercentList", ArrayList(moPercentList))
        intent.putExtra("PercentList", ArrayList(percentList))
        intent.putStringArrayListExtra("modifiedKcalListText", ArrayList(kcalList))
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
    private fun checkKeywordOrder(keywordList: List<String>): Boolean {
        val targetKeywords = listOf("나트", "탄수화", "당류", "지방", "트랜스", "포화", "콜레스", "단백질")


//        textView.append(keywordList.toString())

        var targetIndex = 0
        for (keyword in keywordList) {
            val matchingKeyword = targetKeywords.getOrNull(targetIndex)
            if (matchingKeyword != null && keyword.contains(matchingKeyword)) {
                targetIndex++
            }

            if (targetIndex == targetKeywords.size) {
//                textView.append("true")
                return true
            }
        }

        return false
    }

    private fun isValidData(): Boolean { // 퍼센트와 칼로리 유효성 판단

        return percentList.size == 7 && kcalList.size == 1 && percentList.all { it.isNotEmpty() } && kcalList.all { it.isNotEmpty() } && koreanCharactersListmodi.all { it.isNotEmpty() } && moPercentList.size == 7
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
            koreanCharactersList.clear()
            koreanCharactersListmodi.clear()
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
//                        textView.append("감지된 텍스트: $extractedText\n")
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
                                        koreanCharactersList.add(elementText)
                                        // replaceKoreanCharacters(koreanCharactersSet)
                                    }
                                }
                            }
                            val lineText = line.text
                            val lineInfo = "라인 텍스트: $lineText" // 전체 인식한 라인 텍스트 출력
//                            runOnUiThread { // OCR 결과 학인 위해.. 나중에 제거할 예정
//                                textView.append("$lineInfo\n")
//                            }

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
                    koreanCharactersListmodi = koreanCharactersList.distinct().toMutableList()
                    koreanCharactersListmodi = koreanCharactersListmodi.map { it.replace(Regex("[^가-힣]"), "") }.toMutableList()
                    runOnUiThread { // OCR 결과 학인 위해.. 나중에 제거할 예정
//                        textView.append("$koreanCharactersListmodi\n")
//                        textView.append("$percentList\n")
                    }

                }
                .addOnFailureListener { e ->

                    e.printStackTrace()
                    showAlertDialog("네트워크를 연결해주세요 또는 API 연동 중이거나 적합하지 않은 이미지일 수 있습니다.")
                }
        } catch (e: Exception) {
            e.printStackTrace()
            // 추가로 예외 정보를 로그에 출력
            Log.e("wrong", "Exception occurred: ${e.message}", e)
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
            val cleanedPercent = percentNumber.trimStart('0') //선행하는 0을 제거
            percentList.add(cleanedPercent)
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


// % 를 이용하여 g으로 계산
private fun modiPercentList(percentList: List<String>): List<String> {
    if (percentList.size != 7) {
        // 퍼센트 리스트의 길이가 7이 아니면 빈 리스트를 반환
        return emptyList()
    }

    val modifiedList = percentList.mapIndexed { index, percent ->
        // 선행하는 0을 제거
        val cleanedPercent = percent.trimStart('0').toDoubleOrNull() ?: 0.0

        val modifiedPercent = when (index) {
            0 -> ((cleanedPercent * 0.01 * 2000).toInt()).toString()
            1 -> ((cleanedPercent * 0.01 * 324 * 1000).toInt()).toString()
            2 -> ((cleanedPercent * 0.01 * 100 * 1000).toInt()).toString()
            3 -> ((cleanedPercent * 0.01 * 54 * 1000).toInt()).toString()
            4 -> ((cleanedPercent * 0.01 * 15 * 1000).toInt()).toString()
            5 -> ((cleanedPercent * 0.01 * 300).toInt()).toString()
            6 -> ((cleanedPercent * 0.01 * 55 * 1000).toInt()).toString()
            else -> ((cleanedPercent * 0.01 * 2000).toInt()).toString() // 기본값
        }
        modifiedPercent
    }

    return modifiedList
}