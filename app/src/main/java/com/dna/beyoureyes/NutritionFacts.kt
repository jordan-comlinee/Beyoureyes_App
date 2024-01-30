package com.dna.beyoureyes

//========================================================================
// 영양소 정보 관리를 위한 enum 객체
//========================================================================

// 영양소별 주로 쓰이는 질량 단위 구분을 위한 enum
enum class UnitOfMass { MILLIGRAM, GRAM }

//========================================================================
// 영양소 정보 관리를 위한 class들
//========================================================================
// 개별 영양 성분 클래스
class Nutrition(private var milligram:Int, val unit:UnitOfMass) {
    fun getGram(): Int { return milligram / 1000 }
    fun getMilliGram() : Int { return milligram }
    fun getPercentageOfDailyValue(dailyMilli : Int) : Int {
        return ( milligram.toDouble() / dailyMilli.toDouble() * 100).toInt()
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
        this.natrium = Nutrition(natrium, UnitOfMass.MILLIGRAM)
        this.carbs = Nutrition(carbs, UnitOfMass.GRAM)
        this.sugar = Nutrition(sugar, UnitOfMass.GRAM)
        this.protein = Nutrition(protein, UnitOfMass.GRAM)
        this.fat = Nutrition(fat, UnitOfMass.GRAM)
        this.satFat = Nutrition(satFat, UnitOfMass.GRAM)
        this.chol = Nutrition(chol, UnitOfMass.MILLIGRAM)
    }

    constructor(nutritionMap: Map<String, Any?>, calories:Int) : this(){
        setEnergyValue(calories)
        setNutritionValues(nutritionMap)
    }

    constructor(nutriMilliList: IntArray, totalKcal:Int) : this(
        nutriMilliList[0], nutriMilliList[1], nutriMilliList[2], // 나트륨, 탄수화물, 당류
        nutriMilliList[6], // 단백질
        nutriMilliList[3], nutriMilliList[4], nutriMilliList[5], // 지방, 포화지방, 콜레스테롤
        totalKcal
    ) {}

    // =========================================================================
    // get 메소드
    // =========================================================================

    fun getMilligramByNutriLabel(label:String) : Int {
        when(label) {
            "나트륨" -> return natrium?.getMilliGram() ?: 0
            "탄수화물" -> return carbs?.getMilliGram() ?: 0
            "당류" -> return sugar?.getMilliGram() ?: 0
            "지방" -> return fat?.getMilliGram() ?: 0
            "포화지방" -> return satFat?.getMilliGram() ?: 0
            "콜레스테롤" -> return chol?.getMilliGram() ?: 0
            "단백질" -> return protein?.getMilliGram() ?: 0
            else -> return -1
        }
    }

    fun getGramByNutriLabel(label:String) : Int {
        when(label) {
            "나트륨" -> return natrium?.getGram() ?: 0
            "탄수화물" -> return carbs?.getGram() ?: 0
            "당류" -> return sugar?.getGram() ?: 0
            "지방" -> return fat?.getGram() ?: 0
            "포화지방" -> return satFat?.getGram() ?: 0
            "콜레스테롤" -> return chol?.getGram() ?: 0
            "단백질" -> return protein?.getMilliGram() ?: 0
            else -> return -1
        }
    }

    fun getPercentOfDailyValueByNutriLabel(label:String, DVs: NutrientDailyValues) : Int {
        when(label) {
            "나트륨" -> return natrium?.getPercentageOfDailyValue(DVs.natrium.getMilliGram()) ?: 0
            "탄수화물" -> return carbs?.getPercentageOfDailyValue(DVs.carbs.getMilliGram()) ?: 0
            "당류" -> return sugar?.getPercentageOfDailyValue(DVs.sugar.getMilliGram()) ?: 0
            "지방" -> return fat?.getPercentageOfDailyValue(DVs.fat.getMilliGram()) ?: 0
            "포화지방" -> return satFat?.getPercentageOfDailyValue(DVs.satFat.getMilliGram()) ?: 0
            "콜레스테롤" -> return chol?.getPercentageOfDailyValue(DVs.chol.getMilliGram()) ?: 0
            "단백질" -> return protein?.getPercentageOfDailyValue(DVs.protein.getMilliGram()) ?: 0
            else -> return -1
        }
    }

    // =========================================================================
    // set 메소드
    // =========================================================================
    // firebaseDB에서 읽어온 nutrition key-value 쌍(<String, Any?>)을 전달받아 해당하는 값 설정
    // Int?가 아니라 Any?로 매핑하도록 고려한 이유는... 혹시 정수형으로 저장되지 않은 데이터들이 있을 경우에 대응하기 위해

    fun setNutritionValues(nutritionMap: Map<String, Any?>) {
        // firebaseDB 필드명 수정 시 아래 nutritionMap의 키값명 수정 필요!!(동일하게)
        anyToInt(nutritionMap["나트륨"])?.let {
            this.natrium = Nutrition(it, UnitOfMass.MILLIGRAM)
        }
        anyToInt(nutritionMap["탄수화물"])?.let {
            this.carbs = Nutrition(it, UnitOfMass.GRAM)
        }
        anyToInt(nutritionMap["당류"])?.let {
            this.sugar = Nutrition(it, UnitOfMass.GRAM)
        }
        anyToInt(nutritionMap["단백질"])?.let {
            this.protein = Nutrition(it, UnitOfMass.GRAM)
        }
        anyToInt(nutritionMap["지방"])?.let {
            this.fat = Nutrition(it, UnitOfMass.GRAM)
        }
        anyToInt(nutritionMap["포화지방"])?.let {
            this.satFat = Nutrition(it, UnitOfMass.GRAM)
        }
        anyToInt(nutritionMap["콜레스테롤"])?.let {
            this.chol = Nutrition(it, UnitOfMass.MILLIGRAM)
        }
    }

    fun setEnergyValue(energy: Int) {
        this.energy = energy
    }

    fun setEnergyValue(energy: Any) {
        anyToInt(energy)?.let {
            this.energy = it
        }
    }

    // =========================================================================
    // 클래스 내부에서 반복 사용되는 기능을 위한 기타 helper 메소드들...
    // =========================================================================

    // Any? 타입을 Nutrition?로 변환하는 메소드
    private fun anyToInt(any:Any?) : Int? {

        // FirebaseDB에서 가져온 Any? 타입의 값이 어떤 형식인지(Long/Double/Int/else) 체크하고 적절한 값으로 변환해서 반환
        when(any) {
            is Long -> {
                return any.toInt()
            }
            is Double -> {

                return any.toInt()
            }
            is Int -> {
                return any
            }
            else -> return null
        }
    }

    operator fun plus(b: NutritionFacts) : NutritionFacts {

        var nat = 0
        this.natrium?.let {a ->
            nat += a.getMilliGram()
        }
        b.natrium?.let {b ->
            nat += b.getMilliGram()
        }

        var carb = 0
        this.carbs?.let {a ->
            carb += a.getMilliGram()
        }
        b.carbs?.let {b ->
            carb += b.getMilliGram()
        }

        var sug = 0
        this.sugar?.let {a ->
            sug += a.getMilliGram()
        }
        b.sugar?.let {b ->
            sug += b.getMilliGram()
        }

        var prot = 0
        this.protein?.let {a ->
            prot += a.getMilliGram()
        }
        b.protein?.let {b ->
            prot += b.getMilliGram()
        }

        var fat = 0
        this.fat?.let {a ->
            fat += a.getMilliGram()
        }
        b.fat?.let {b ->
            fat += b.getMilliGram()
        }

        var sf = 0
        this.satFat?.let {a ->
            sf += a.getMilliGram()
        }
        b.satFat?.let {b ->
            sf += b.getMilliGram()
        }

        var ch = 0
        this.chol?.let {a ->
            ch += a.getMilliGram()
        }
        b.chol?.let {b ->
            ch += b.getMilliGram()
        }

        var energy = 0
        this.energy?.let {a ->
            energy += a
        }
        b.energy?.let {b ->
            energy += b
        }

        return NutritionFacts(nat, carb, sug, prot, fat, sf, ch, energy)
    }

}