package com.dna.beyoureyes

//========================================================================
// 일일 권장량 관리를 위한 인터페이스(질량 단위 변환 편하게 하기 위해)
//========================================================================
interface DailyValue {
    val unit:UnitOfMass
    var dailyValue: Int
    var intakeRange : Map<IntakeRange, IntProgression>?
    fun getGram() : Int { return dailyValue / 1000 }
    fun getMilliGram() : Int { return dailyValue }
    fun getDVString() : String {
        var str = ""
        when(unit){
            UnitOfMass.GRAM -> str= getGram().toString() + "g"
            UnitOfMass.MILLIGRAM -> str = getMilliGram().toString() + "mg"
        }
        return str
    }

    fun isLack(milliIntake: Int) : Boolean {
        intakeRange?.let {
            for(key in it.keys){
                it[key]?.let{ range ->
                    if (milliIntake in range) {
                        return key == IntakeRange.LACK
                    }
                }
            }
        }
        return false
    }

    fun isLess(milliIntake: Int) : Boolean {
        intakeRange?.let {
            for(key in it.keys){
                it[key]?.let{ range ->
                    if (milliIntake in range) {
                        return key == IntakeRange.LESS
                    }
                }
            }
        }
        return false
    }

    fun isEnough(milliIntake: Int) : Boolean {
        intakeRange?.let {
            for(key in it.keys){
                it[key]?.let{ range ->
                    if (milliIntake in range) {
                        return key == IntakeRange.ENOUGH
                    }
                }
            }
        }
        return false
    }
    fun isOver(milliIntake: Int) : Boolean {
        intakeRange?.let {
            for(key in it.keys){
                it[key]?.let{ range ->
                    if (milliIntake in range) {
                        return key == IntakeRange.OVER
                    }
                }
            }
        }
        return false
    }

    fun isWarning(milliIntake:Int) : Boolean {
        intakeRange?.let {
            for(key in it.keys){
                it[key]?.let{ range ->
                    if (milliIntake in range) {
                        return key.status == Status.WARNING
                    }
                }
            }
        }
        return false
    }

    fun isCaution(milliIntake:Int) : Boolean {
        intakeRange?.let {
            for(key in it.keys){
                it[key]?.let{ range ->
                    if (milliIntake in range) {
                        return key.status == Status.CAUTION
                    }
                }
            }
        }
        return false
    }

    fun isSatisfied(milliIntake:Int) : Boolean {
        intakeRange?.let {
            for(key in it.keys){
                it[key]?.let{ range ->
                    if (milliIntake in range) {
                        return key.status == Status.SATISFIED
                    }
                }
            }
        }
        return false
    }

    // 섭취량 상태 구분 -> 경고/권장/주의
    fun getIntakeStatus(milliIntake: Int) : IntakeRange? {
        intakeRange?.let {
            for(key in it.keys){
                it[key]?.let{ range ->
                    if (milliIntake in range) {
                        return key
                    }
                }
            }
        }
        return null
    }
}
//========================================================================
// DRI 클래스 구현을 위한 인터페이스 -> 섭취량을 제한하는 범위에 따라 3가지로
//========================================================================

/*
    섭취량에 대한 상한, 하한 기준이 모두 존재하는 경우 - 나트륨, 당, 지방
    - 하한 미달 - 주의(노랑)
    - 하한 ~ 상한 - 적정(초록)
    - 상한 초과 - 경고(빨강)
     */
interface DRIwithClosedRange : DailyValue {
    fun setIntakeRange(lower:Int, upper:Int){
        intakeRange = mapOf( // 섭취량 범위 조건 설정
            IntakeRange.LESS to 0..< lower, // 미달(주의)
            IntakeRange.ENOUGH to lower..upper, // 적정
            IntakeRange.OVER to (upper+1)..Int.MAX_VALUE // 초과(경고)
        )
    }
}

/*
    섭취량에 대한 하한 기준만 존재하는 경우 - 탄수화물, 단백질
    - 하한 미달 - 경고(빨강)
    - 하한 ~ 적정량 미만 - 주의(노랑)
    - 적정량 이상 - 적정(초록)
     */
interface DRIwithOpenEndRange : DailyValue {
    fun setIntakeRange(lower:Int, upper:Int){
        intakeRange = mapOf( // 섭취량 범위 조건 설정
            IntakeRange.LACK to 0..<lower, // 미달(경고)
            IntakeRange.LESS to lower..<upper, // 미달(주의)
            IntakeRange.ENOUGH to upper..Int.MAX_VALUE // 적정
        )
    }
}

/*
    섭취량에 대한 상한 기준만 존재하는 경우 - 포화지방, 콜레스테롤
    - 상한 미만 - 적정(초록)
    - 상한 이상 - 경고(빨강)
*/
interface DRIwithOpenStartRange : DailyValue {
    fun setIntakeRange(upper:Int){
        intakeRange = mapOf( // 섭취량 범위 조건 설정
            IntakeRange.ENOUGH to 0..< upper, // 적정
            IntakeRange.OVER to upper..Int.MAX_VALUE // 초과(경고)
        )
    }

    override fun getDVString() : String {
        return super.getDVString() + " 미만"
    }
}
