package com.example.beyoureyes

// 개별 영양 성분 클래스
class Nutrition(private var milligram:Int) {
    fun getGram(): Int { return milligram / 1000 }
    fun getMilliGram() : Int { return milligram }
    fun getPercentageOfDailyValue(dailyMilli : Int) : Int {
        return ( milligram.toDouble() / dailyMilli.toDouble() ).toInt() * 100
    }
}

// 종합 영양 성분표 정보를 관리하는 클래스
class NutritionFacts() {
    // =========================================================================
    // 생성자
    // =========================================================================

    // primary 생성자 - 모든 값 null로 초기화
    var energy : Int? = null
    var natrium : Nutrition? = null
    var carbs : Nutrition? = null
    var sugar : Nutrition? = null
    var protein : Nutrition? = null
    var fat : Nutrition? = null
    var satFat : Nutrition? = null
    var chol : Nutrition? = null

    // secondary 생성자 - 각 성분값을 int로 받아 생성
    constructor(natrium:Int, carbs:Int, sugar:Int, protein:Int, fat:Int, satFat:Int,
                chol:Int, energy:Int) : this(){
        this.energy = energy
        this.natrium = Nutrition(natrium)
        this.carbs = Nutrition(carbs)
        this.sugar = Nutrition(sugar)
        this.protein = Nutrition(protein)
        this.fat = Nutrition(fat)
        this.satFat = Nutrition(satFat)
        this.chol = Nutrition(chol)
    }

    // =========================================================================
    // set 메소드
    // =========================================================================
    // firebaseDB에서 읽어온 nutrition key-value 쌍(<String, Any?>)을 전달받아 해당하는 값 설정
    // Int?가 아니라 Any?로 매핑하도록 고려한 이유는... 혹시 정수형으로 저장되지 않은 데이터들이 있을 경우에 대응하기 위해

    fun setNutritionValues(nutritionMap: Map<String, Any?>) {
        // firebaseDB 필드명 수정 시 아래 nutritionMap의 키값명 수정 필요!!(동일하게)
        this.natrium = anyToNutrition(nutritionMap["natrium"])
        this.carbs = anyToNutrition(nutritionMap["carbs"])
        this.sugar = anyToNutrition(nutritionMap["sugar"])
        this.protein = anyToNutrition(nutritionMap["protein"])
        this.fat = anyToNutrition(nutritionMap["fat"])
        this.satFat = anyToNutrition(nutritionMap["saturatedFat"])
        this.chol = anyToNutrition(nutritionMap["cholesterol"])
    }

    fun setEnergyValue(energy: Int) {
        this.energy = energy
    }

    // =========================================================================
    // 클래스 내부에서 반복 사용되는 기능을 위한 기타 helper 메소드들...
    // =========================================================================

    // Any? 타입을 Nutrition?로 변환하는 메소드
    private fun anyToNutrition(any:Any?) : Nutrition? {

        // FirebaseDB에서 가져온 Any? 타입의 값이 어떤 형식인지(Long/Double/Int/else) 체크하고 적절한 값으로 변환해서 반환
        when(any) {
            is Long -> return Nutrition(any.toInt())
            is Double -> return Nutrition(any.toInt())
            is Int -> return Nutrition(any)
            else -> return null
        }
    }
}