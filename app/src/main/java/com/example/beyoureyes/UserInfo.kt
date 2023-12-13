package com.example.beyoureyes

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