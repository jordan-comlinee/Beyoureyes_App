package com.dna.beyoureyes

import com.google.firebase.firestore.QueryDocumentSnapshot

// 유저 정보 관리에 필요한 성별 enum 상수
enum class Gender {
    WOMAN, MAN
}

// 기본 생성자 - 각 속성 값을 직접 전달받음. diseas, allergic은 디폴트값으로 null 세팅.
class UserInfo (
    var age : Int,                  // 사용자 나이
    var gender : Int,               // 사용자 성별
    var disease : MutableSet<String>?,   // 사용자 질병 정보(nullable - 해당사항 없을 수 있으므로)
    var allergic : MutableSet<String>?   // 사용자 알레르기 정보(nullable - 해당사항 없을 수 있으므로
) {
    constructor(age:Int, gender:Int, disease:Array<String>?, allergic:Array<String>?)
            :this(age, gender, disease?.toMutableSet(), allergic?.toMutableSet())

    //  사용자 맞춤 권장량 정보를 제공하는 get 메소드
    fun getDailyValues() : NutrientDailyValues {
        return NutrientDailyValues(gender, age, disease?.toTypedArray())
    }

    fun hasDisease() : Boolean {
        return disease != null
    }

    fun getNutrisToCare() : Array<String> {
        var list = mutableListOf<String>()
        disease?.forEach {
            when (it) {
                "고지혈증" -> list.addAll(arrayOf("지방", "포화지방", "콜레스테롤"))
                "고혈압" -> list.addAll(arrayOf("나트륨", "포화지방", "콜레스테롤"))
                "당뇨" -> list.addAll(arrayOf("당류", "콜레스테롤"))
            }
        }
        val set = list.toSet() // 중복 없애기
        return set.toTypedArray()
    }
    companion object {
        fun parseFirebaseDoc(document: QueryDocumentSnapshot) : UserInfo? {
            val age = document.data.get("userAge") as? Long
            val sex = document.data.get("userSex") as? Long
            val diseaseList = document.data.get("userDisease") as ArrayList<String>
            val allergicList = document.data.get("userAllergic") as ArrayList<String>
            age?.let { age ->
                sex?.let {sex->
                    var disease: Array<String>? = null
                    var allergic: Array<String>? = null
                    if ( diseaseList.size > 0) disease = diseaseList.toTypedArray()
                    if (allergicList.size > 0) allergic = allergicList.toTypedArray()
                    return UserInfo(age.toInt(), sex.toInt(), disease, allergic)
                }
            }
            return null
        }
    }
}