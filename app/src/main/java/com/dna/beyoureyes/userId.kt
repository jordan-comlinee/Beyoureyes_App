package com.dna.beyoureyes


import android.app.Application

class userId : Application() {
    var userId: String? = null
}


// id만 저장하는 게 아니므로 객체명을 AppUser로 변경(이름 중복을 피하기 위해 앞에 App 붙임)
object AppUser {
    // 객체를 id, userInfo(기존 클래스) 속성을 갖도록 정의
    var id : String? = null
    var info : UserInfo? = null
}
