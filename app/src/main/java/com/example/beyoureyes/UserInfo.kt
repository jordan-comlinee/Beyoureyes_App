package com.example.beyoureyes

// 유저 정보 관리에 필요한 성별 enum 상수
enum class Gender {
    WOMAN, MAN
}

/*
클래스 기본 사항(기존에서 수정사항 있음)
- 유저 정보를 객체로 관리하기 위한 클래스

[수정 사항]
- 수정 1. 생성자 형태를 코틀린식 기본 생성자로 수정
- 수정 2. 속성명 간결화: 객체 자체가 user니까 앞에 user를 빼도 될 것 같아서 제거
- 수정 3. 속성을 val 타입으로 변경: 읽기 전용
- 수정 4. 나이, 성별 nullable 속성 제거: 나이, 성별을 공란으로 둘 일이 없으니...

*/
class UserInfo {
    private lateinit var userId : String
    private var userAge : Int? = null // 사용자 나이
    private var userDisease : Array<String>? = null // 사용자 질병 정보(nullable)
    private var userAllergic : Array<String>? = null // 사용자 알레르기 정보(nullable)

    constructor(userId : String ,userAge : Int, userDisease : Array<String>?, userAllergic : Array<String>?){
        this.userId = userId
        this.userAge = userAge
        this.userDisease = userDisease
        this.userAllergic = userAllergic
    }

}