package com.dna.beyoureyes

import android.util.Log

//========================================================================
// 섭취 범위 관리를 위한 enum 객체들
//========================================================================

// 섭취 상태 평가를 위한 enum 객체 (주의-경고-적정)
enum class Status(val colorRID: Int) {
    WARNING(R.color.highlight),    // 경고 - 빨간색 표시
    CAUTION(R.color.chartyellow),    // 주의 - 노란색 표시
    SATISFIED(R.color.green)   // 적정 - 초록색 표시
}

// 섭취량이 기준치 미달인지 초과인지 분류하고 그에 따른 평가를 구분하기 위한 enum 객체
enum class IntakeRange(val status: Status) {
    LACK(Status.WARNING),       // 기준치 미달(경고)
    LESS(Status.CAUTION),       // 기준치 약간 미달(주의)
    ENOUGH(Status.SATISFIED),   // 기준치 충족(양호)
    OVER(Status.WARNING)        // 기준치 초과(경고)
}

//========================================================================
// 영양성분별 DRI = Dietary Reference Intakes 클래스 -> 식이조절을 위한 참고 섭취량 관리 클래스
//========================================================================
class NatriumDRI() : DailyValue, DRIwithClosedRange {

    // =========================================================================
    // 생성자
    // =========================================================================

    // primary 생성자 - 일일 권장량만 기본값(식품 표시 기준)으로 설정. 맞춤 정보는 null로 초기화
    override val unit = UnitOfMass.MILLIGRAM
    override var dailyValue : Int = 2000 // 일일 권장량 mg
    override var intakeRange : Map<IntakeRange, IntProgression>? = null // 섭취량 범위 기준맵
    private var adequateIntake : Int? = null // 충분 섭취량
    private var upperIntake : Int? = null // 상한 섭취량

    // secondary 생성자 - 유저 정보 객체를 받아 맞춤 권장량을 설정
    constructor(age:Int, disease: Array<String>?) : this() {
        personalize(age, disease)
    }

    // =========================================================================
    // set 메소드
    // =========================================================================
    fun personalize(age:Int, disease: Array<String>?) {
        // 사용자 맞춤 충분 섭취량, CDPR 값 계산
        setAdequateIntake(age)
        setUpperIntakeLevel(age, disease)

        // 이를 바탕으로 일일 권장량 및 경고/주의/적정 섭취량 범위 기준 설정
        adequateIntake?.let {  ai ->
            dailyValue = ai
            upperIntake?.let { ui ->
                super.setIntakeRange(ai, ui)
            }
        }

        Log.d("dv", intakeRange.toString())
    }

    // 충분 섭취량
    private fun setAdequateIntake(age: Int) {
        adequateIntake =
            when(age){
                in 15..64 -> 1500
                in 65..74 -> 1300
                in 75..Int.MAX_VALUE -> 1100
                else -> 1500 // 14세 이하?
            }
    }

    // 상한 섭취량 = CDPR로 설정
    private fun setUpperIntakeLevel(age: Int, disease: Array<String>?){
        upperIntake =
            when(age){
                in 15..64 -> 2300
                in 65..74 -> 2100
                in 75..Int.MAX_VALUE -> 1700
                else -> 2300 // 14세 이하?
            }
        if( disease != null && disease.contains("고혈압")) {
            val userUpperIntake = (5000 * 0.4).toInt() // 1일 소금 섭취량 5g 기준
            upperIntake?.let {
                if (it > userUpperIntake)
                    upperIntake = userUpperIntake
            }
        }
    }

}
class SugarDRI() : DailyValue, DRIwithClosedRange {

    // =========================================================================
    // 생성자
    // =========================================================================

    // primary 생성자 - 일일 권장량만 기본값(식품 표시 기준)으로 설정. 맞춤 정보는 null로 초기화
    override val unit = UnitOfMass.GRAM
    override var dailyValue : Int = 100000 // 일일 권장량 mg
    override var intakeRange : Map<IntakeRange, IntProgression>? = null // 섭취량 범위 기준맵
    private var recommendedAllowance : Int? = null // 권장 섭취량
    private var upperIntake : Int? = null // 상한 섭취량

    // secondary 생성자 - 유저 정보 객체를 받아 맞춤 권장량을 설정
    constructor( energyReq:Int ) : this() {
        personalize(energyReq)
    }

    // =========================================================================
    // 메소드
    // =========================================================================
    fun personalize(energyReq:Int) {
        // 사용자 맞춤 권장섭취량, 상한섭취량 설정
        setRecommendedDietaryAllowance(energyReq)
        setUpperIntakeLevel(energyReq)

        // 이를 바탕으로 일일 권장량 및 경고/주의/적정 섭취량 범위 기준 설정
        recommendedAllowance?.let {  rda ->
            upperIntake?.let { ui ->
                dailyValue = ui
                super.setIntakeRange(rda, ui)
            }
        }
    }

    // 권장 섭취량 = 에너지 섭취 제한 비율(최소값)
    private fun setRecommendedDietaryAllowance(energyReq:Int) {
        recommendedAllowance = ((energyReq * 0.1) / 4).toInt() * 1000
    }

    // 상한 섭취량 = 에너지 섭취 제한 비율(최대값)
    private fun setUpperIntakeLevel(energyReq:Int){
        upperIntake = ((energyReq * 0.2) / 4).toInt() * 1000
    }

}
class FatDRI() : DailyValue, DRIwithClosedRange {

    // =========================================================================
    // 생성자
    // =========================================================================

    // primary 생성자 - 일일 권장량만 기본값(식품 표시 기준)으로 설정. 맞춤 정보는 null로 초기화
    override val unit = UnitOfMass.GRAM
    override var dailyValue : Int = 54000 // 일일 권장량 mg
    override var intakeRange : Map<IntakeRange, IntProgression>? = null // 섭취량 범위 기준맵
    private var recommendedAllowance : Int? = null // 권장 섭취량
    private var upperIntake : Int? = null // 상한 섭취량

    // secondary 생성자 - 유저 정보 객체를 받아 맞춤 권장량을 설정
    constructor( energyReq : Int, disease: Array<String>? ) : this() {
        personalize(energyReq, disease)
    }

    // =========================================================================
    // 메소드
    // =========================================================================
    fun personalize(energyReq : Int, disease: Array<String>?) {
        // 사용자 맞춤 권장섭취량, 상한섭취량 설정
        setRecommendedDietaryAllowance(energyReq, disease)
        setUpperIntakeLevel(energyReq, disease)

        // 이를 바탕으로 일일 권장량 및 경고/주의/적정 섭취량 범위 기준 설정
        recommendedAllowance?.let {  rda ->
            upperIntake?.let { ui ->
                dailyValue = ui
                super.setIntakeRange(rda, ui)
            }
        }
    }

    // 권장 섭취량 = 에너지 섭취 제한 비율(최소값)
    private fun setRecommendedDietaryAllowance(energyReq:Int, disease: Array<String>?) {
        if( disease != null && disease.contains("고지혈증"))
            recommendedAllowance = ((energyReq * 0.15) / 9).toInt() * 1000
        else
            recommendedAllowance = ((energyReq * 0.15) / 9).toInt() * 1000
    }

    // 상한 섭취량 = 에너지 섭취 제한 비율(최대값)
    private fun setUpperIntakeLevel(energyReq:Int, disease: Array<String>?) {
        if( disease != null && disease.contains("고지혈증"))
            upperIntake = ((energyReq * 0.2) / 9).toInt() * 1000
        else
            upperIntake = ((energyReq * 0.3) / 9).toInt() * 1000
    }

}
class CarbsDRI() : DailyValue, DRIwithOpenEndRange {

    // =========================================================================
    // 생성자
    // =========================================================================

    // primary 생성자 - 일일 권장량만 기본값(식품 표시 기준)으로 설정. 맞춤 정보는 null로 초기화
    override val unit = UnitOfMass.GRAM
    override var dailyValue : Int = 324000 // 일일 권장량 mg
    override var intakeRange : Map<IntakeRange, IntProgression>? = null // 섭취량 범위 기준맵
    private var averageRequirement : Int = 100000 // 평균 필요량
    private var recommendedAllowance : Int = 130000 // 권장 섭취량

    // =========================================================================
    // 메소드
    // =========================================================================
    fun personalize() {
        dailyValue = recommendedAllowance
        super.setIntakeRange(averageRequirement, recommendedAllowance)
    }

}
class ProteinDRI() : DailyValue, DRIwithOpenEndRange {

    // =========================================================================
    // 생성자
    // =========================================================================

    // primary 생성자 - 일일 권장량만 기본값(식품 표시 기준)으로 설정. 맞춤 정보는 null로 초기화
    override val unit = UnitOfMass.GRAM
    override var dailyValue : Int = 55000 // 일일 권장량 mg
    override var intakeRange : Map<IntakeRange, IntProgression>? = null // 섭취량 범위 기준맵
    private var averageRequirement : Int = 100000 // 평균 필요량
    private var recommendedAllowance : Int = 130000 // 권장 섭취량

    // secondary 생성자 - 유저 정보 객체를 받아 맞춤 권장량을 설정
    constructor( gender:Int, age:Int ) : this() {
        personalize(gender, age)
    }

    // =========================================================================
    // 메소드
    // =========================================================================
    fun personalize(gender:Int, age:Int ) {
        // 사용자 맞춤 평균 필요량, 권장섭취량 설정
        setEstimatedAverageRequirement(gender, age)
        setRecommendedDietaryAllowance(gender, age)

        // 이를 바탕으로 일일 권장량 및 경고/주의/적정 섭취량 범위 기준 설정
        recommendedAllowance?.let {  rda ->
            averageRequirement?.let { ear ->
                dailyValue = rda
                super.setIntakeRange(ear, rda)
            }
        }
    }

    // 평균 필요량
    private fun setEstimatedAverageRequirement(gender:Int, age:Int){
        if (gender == Gender.WOMAN.ordinal) { // 여성
            averageRequirement = when(age){
                in 15..29 -> 45000
                in 30..Int.MAX_VALUE -> 40000
                else -> 45000
            }
        } else { // 남성
            averageRequirement = when(age){
                in 15..18 -> 55000
                in 19..Int.MAX_VALUE -> 50000
                else -> 55000
            }
        }
    }

    // 권장 섭취량
    private fun setRecommendedDietaryAllowance(gender:Int, age:Int) {
        if (gender == Gender.WOMAN.ordinal) { // 여성
            recommendedAllowance = when(age){
                in 15..29 -> 55000
                in 30..Int.MAX_VALUE -> 50000
                else -> 55000
            }
        } else { // 남성
            recommendedAllowance = when(age){
                in 15..49 -> 65000
                in 50..Int.MAX_VALUE -> 60000
                else -> 65000
            }
        }
    }

}
class SaturatedFatDRI() : DailyValue, DRIwithOpenStartRange {

    // =========================================================================
    // 생성자
    // =========================================================================

    // primary 생성자 - 일일 권장량만 기본값(식품 표시 기준)으로 설정. 맞춤 정보는 null로 초기화
    override val unit = UnitOfMass.GRAM
    override var dailyValue : Int = 15000 // 일일 권장량 mg
    override var intakeRange : Map<IntakeRange, IntProgression>? = null // 섭취량 범위 기준맵
    private var upperIntake : Int? = null // 상한 섭취량

    // secondary 생성자 - 유저 정보 객체를 받아 맞춤 권장량을 설정
    constructor( age:Int, energyReq: Int, disease: Array<String>? ) : this() {
        personalize(age, energyReq, disease)
    }

    // =========================================================================
    // 메소드
    // =========================================================================
    fun personalize(age:Int, energyReq: Int, disease: Array<String>?) {
        // 사용자 맞춤 상한 섭취량 설정
        setUpperIntakeLevel(age, energyReq, disease)

        // 이를 바탕으로 일일 권장량 및 경고/주의/적정 섭취량 범위 기준 설정
        upperIntake?.let {  ui ->
            dailyValue = ui
            super.setIntakeRange(ui)
        }
    }

    // 상한 섭취량
    private fun setUpperIntakeLevel(age:Int, energyReq: Int, disease: Array<String>?){
        var ratio = 0.0
        if( disease != null && disease.contains("고지혈증"))
            ratio = 0.07
        else
            ratio = when(age) {
                in 15..18 -> 0.08
                in 19..Int.MAX_VALUE -> 0.07
                else -> 0.08
            }
        upperIntake = ((energyReq * ratio) / 9).toInt() * 1000

    }

}
class CholesterolDRI() : DailyValue, DRIwithOpenStartRange {

    // =========================================================================
    // 생성자
    // =========================================================================

    // primary 생성자 - 일일 권장량만 기본값(식품 표시 기준)으로 설정. 맞춤 정보는 null로 초기화
    override val unit = UnitOfMass.MILLIGRAM
    override var dailyValue : Int = 300 // 일일 권장량 mg
    override var intakeRange : Map<IntakeRange, IntProgression>? = null // 섭취량 범위 기준맵
    private var upperIntake : Int? = null // 상한 섭취량

    // secondary 생성자 - 유저 정보 객체를 받아 맞춤 권장량을 설정
    constructor( disease: Array<String>? ) : this() {
        personalize(disease)
    }

    // =========================================================================
    // 메소드
    // =========================================================================
    fun personalize(disease: Array<String>?) {
        // 사용자 맞춤 상한 섭취량 설정
        setUpperIntakeLevel(disease)

        // 이를 바탕으로 일일 권장량 및 경고/주의/적정 섭취량 범위 기준 설정
        upperIntake?.let {  ui ->
            dailyValue = ui
            super.setIntakeRange(ui)
        }
    }

    // 상한 섭취량
    private fun setUpperIntakeLevel(disease: Array<String>?){
        if( disease != null &&
            (disease.contains("고지혈증") || disease.contains("고혈압"))){
            upperIntake = 200
        } else {
            upperIntake = 300
        }
    }

}


//========================================================================
// 7가지 영양성분 + 에너지 섭취량에 대한 통합 권장량 관리 클래스
//========================================================================
class NutrientDailyValues() {
    // =========================================================================
    // 생성자
    // =========================================================================
    // primary 생성자 - 인자 없음.
    // 모든 일일 권장량을 기본값(식품 표시 기준)으로 설정. 정보가 없으므로 맞춤 정보는 null로 초기화
    var energy = 2000
    var natrium = NatriumDRI()
    var carbs = CarbsDRI()
    var sugar = SugarDRI()
    var protein = ProteinDRI()
    var fat = FatDRI()
    var satFat = SaturatedFatDRI()
    var chol = CholesterolDRI()

    // secondary 생성자 - 유저 정보 객체를 받아 맞춤 권장량을 설정
    constructor( gender: Int, age:Int, disease: Array<String>? ) : this() {
        personalizeAllProperties(gender, age, disease)
    }

    // =========================================================================
    // set 메소드
    // =========================================================================

    fun personalizeAllProperties(gender: Int, age:Int, disease: Array<String>?){
        setPersonalizedEnergy(gender, age)
        natrium.personalize(age, disease)
        carbs.personalize()
        sugar.personalize(this.energy)
        protein.personalize(gender, age)
        fat.personalize(this.energy, disease)
        satFat.personalize(age, this.energy, disease)
        chol.personalize(disease)
    }

    fun setPersonalizedEnergy(gender: Int, age: Int){
        if (gender == Gender.WOMAN.ordinal) { // 여성
            energy = when(age){
                in 15..29 -> 2000
                in 30..49 -> 1900
                in 50..64 -> 1700
                in 65..74 -> 1600
                in 75..Int.MAX_VALUE -> 1500
                else -> 2000
            }
        } else { // 남성
            energy = when(age){
                in 15..18 -> 2700
                in 19..29 -> 2600
                in 30..49 -> 2500
                in 50..64 -> 2200
                in 65..74 -> 2000
                in 75..Int.MAX_VALUE -> 1900
                else -> 2700
            }
        }
    }
}
