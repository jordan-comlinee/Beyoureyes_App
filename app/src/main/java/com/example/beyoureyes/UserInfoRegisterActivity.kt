package com.example.beyoureyes

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.material.chip.Chip
import android.content.Intent
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import java.io.Serializable


val diseaseList : List<String> = listOf("고혈압", "고지혈증", "당뇨")
val allergyList : List<String> = listOf("메밀", "밀", "콩", "호두", "땅콩", "복숭아", "토마토", "돼지고기", "난류", "우유", "닭고기", "쇠고기", "새우", "고등어", "홍합", "전복", "굴", "조개류", "게", "오징어", "아황산")

class UserInfoRegisterActivity : AppCompatActivity() {
    private var clickedDisease : MutableList<Boolean> = mutableListOf(false, false, false)
    private var clickedAllergic : MutableList<Boolean> = MutableList(21) { false }
    private val userDiseaseList : ArrayList<String> = arrayListOf()
    private val userAllergyList : ArrayList<String> = arrayListOf()
    var userSex : Int = 0;

    private var userInfoCheck : Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info_register)
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)

        //val userInfoRegisterTitle = findViewById<TextView>(R.id.userInfoRegisterTitle)

        val age : EditText = findViewById(R.id.editAge)

        val sexSwitch : Switch = findViewById(R.id.sexSwitch)

        // 질환 칩
        val chip0 = findViewById<Chip>(R.id.chip0)
        val chip1 = findViewById<Chip>(R.id.chip1)
        val chip2 = findViewById<Chip>(R.id.chip2)
        // 알러지 칩
        val chip00 = findViewById<Chip>(R.id.chip2_0)
        val chip01 = findViewById<Chip>(R.id.chip2_1)
        val chip02 = findViewById<Chip>(R.id.chip2_2)
        val chip03 = findViewById<Chip>(R.id.chip2_3)
        val chip04 = findViewById<Chip>(R.id.chip2_4)
        val chip05 = findViewById<Chip>(R.id.chip2_5)
        val chip06 = findViewById<Chip>(R.id.chip2_6)
        val chip07 = findViewById<Chip>(R.id.chip2_7)
        val chip08 = findViewById<Chip>(R.id.chip2_8)
        val chip09 = findViewById<Chip>(R.id.chip2_9)
        val chip10 = findViewById<Chip>(R.id.chip2_10)
        val chip11 = findViewById<Chip>(R.id.chip2_11)
        val chip12 = findViewById<Chip>(R.id.chip2_12)
        val chip13 = findViewById<Chip>(R.id.chip2_13)
        val chip14 = findViewById<Chip>(R.id.chip2_14)
        val chip15 = findViewById<Chip>(R.id.chip2_15)
        val chip16 = findViewById<Chip>(R.id.chip2_16)
        val chip17 = findViewById<Chip>(R.id.chip2_17)
        val chip18 = findViewById<Chip>(R.id.chip2_18)
        val chip19 = findViewById<Chip>(R.id.chip2_19)
        val chip20 = findViewById<Chip>(R.id.chip2_20)

        val usrInfoRegiSaveButton = findViewById<Button>(R.id.usrInfoRegiSaveButton)
        val usrInfoRegiCancelButton = findViewById<Button>(R.id.usrInfoRegiCancelButton)

        //toolBar
        val toolBar = findViewById<Toolbar>(R.id.toolbarDefault)
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        val toolbarBackButton = findViewById<ImageButton>(R.id.toolbarBackBtn)
        setSupportActionBar(toolBar)
        //Toolbar에 앱 이름 표시 제거!!
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbarBackButton.setOnClickListener {
            onBackPressed()
            //overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_enter)
        }


        val diseaseChips = arrayOf(chip0, chip1, chip2)
        val allergyChips = arrayOf(chip00, chip01, chip02, chip03, chip04, chip05, chip06, chip07,
                                    chip08, chip09, chip10, chip11, chip12, chip13, chip14, chip15,
                                    chip16, chip17, chip18, chip19, chip20)



        // 사용자 정보 존재 여부에 따른 등록/수정 구분
        AppUser.info?.let { // 사용자 정보 있음(기존 정보 수정화면)
            toolbarTitle.setText("내 정보 수정하기")

            // 기존 정보를 수정 화면에 반영

            // 사용자 나이
            age.setText(it.age.toString())

            // 사용자 성별
            userSex = it.gender
            sexSwitch.isChecked = (it.gender==Gender.WOMAN.ordinal)

            // 사용자 질환
            it.disease?.let { userDisease ->
                for (chip in diseaseChips) {
                    if (userDisease.contains(chip.text)){
                        chip.isChecked = true
                    }
                }
            }
            // 사용자 알레르기
            it.allergic?.let { userAllergic ->
                for (chip in allergyChips) {
                    if (userAllergic.contains(chip.text)){
                        chip.isChecked = true
                    }
                }
            }
        }?:run{ // 사용자 정보 없음(기존 정보 등록화면)
            toolbarTitle.setText("내 정보 등록하기")
        }

        // 알러지, 질환 칩 클릭 시 색상 설정하는 코드 없애고 레이아웃 상에서 적용해둠

        // 등록하기 버튼 클릭 시 로직
        usrInfoRegiSaveButton.setOnClickListener {
            // 나이 입력 X 시 토스트 메세지 띄움
            if(age.text.toString() == ""){
                Toast.makeText(this@UserInfoRegisterActivity, "나이를 입력해주세요!", Toast.LENGTH_LONG).show()
            }else{ // 나이 입력 내용 유효한지 검사
                val ageInt = age.text.toString().toIntOrNull()
                if (ageInt == null)
                    Toast.makeText(this@UserInfoRegisterActivity, "나이를 숫자로만 입력해주세요!", Toast.LENGTH_LONG).show()
                else
                {
                    if(ageInt !in 15..120)
                        Toast.makeText(this@UserInfoRegisterActivity, "등록 가능한 나이 범위는 15세~120세 입니다.", Toast.LENGTH_LONG).show()
                    // 나이 입력 O 시
                    else {

                        // 성별 정보 반영
                        if (sexSwitch.isChecked)
                            userSex = 0
                        else
                            userSex = 1

                        // 선택한 질환 정보
                        val checkedDisease =
                            (diseaseChips.filter { it.isChecked }).map { it.text.toString() }
                        userDiseaseList.addAll(checkedDisease)

                        // 선택한 알러지 정보
                        val checkedAllergy =
                            (allergyChips.filter { it.isChecked }).map { it.text.toString() }
                        userAllergyList.addAll(checkedAllergy)


                        // 싱글톤 객체 & Firebase DB 업데이트 ----------------------------------
                        // 기존 유저 정보가 있다면 삭제
                        if(AppUser.info != null) {

                            // 나이 정보 반영
                            AppUser.info?.age = ageInt

                            // 성별 정보 반영
                            AppUser.info?.gender = userSex

                            // 질환 정보
                            AppUser.info?.disease?.clear()
                            AppUser.info?.disease?.addAll(checkedDisease)

                            // 알레르기 정보
                            AppUser.info?.allergic?.clear()
                            AppUser.info?.allergic?.addAll(checkedAllergy)


                            Log.d("REGISTERFIRESTORE : ", "DELETE START")
                            deleteData(AppUser.id!!, "userInfo") {
                                // 삭제가 완료되면 이 블록이 실행됨
                                // 여기에서 sendData 함수 호출
                                val userInfo = hashMapOf(
                                    "userID" to AppUser.id!!,
                                    "userAge" to AppUser.info!!.age,
                                    "userSex" to AppUser.info!!.gender,
                                    "userDisease" to userDiseaseList,
                                    "userAllergic" to userAllergyList
                                )
                                sendData(userInfo, "userInfo")
                                userDiseaseList.clear()
                                userAllergyList.clear()

                                val intent = Intent(this, UserInfoActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        else {

                            AppUser.info = UserInfo(
                                ageInt, userSex,
                                checkedDisease.toMutableSet(), checkedAllergy.toMutableSet())

                            // 유저 정보가 없는 경우에는 바로 sendData 함수 호출
                            val userInfo = hashMapOf(
                                "userID" to AppUser.id!!,
                                "userAge" to ageInt,
                                "userSex" to userSex,
                                "userDisease" to userDiseaseList,
                                "userAllergic" to userAllergyList
                            )
                            sendData(userInfo, "userInfo")
                            userDiseaseList.clear()
                            userAllergyList.clear()

                            val intent = Intent(this, UserInfoActivity::class.java)
                            startActivity(intent)
                        }

                    }
                }
            }
        }

        // 취소 버튼 클릭 시
        usrInfoRegiCancelButton.setOnClickListener {
            onBackPressed()
        }

    }

    private fun sendData(userInfo : HashMap<String, Serializable>, collectionName : String){
        val db = Firebase.firestore
        db.collection(collectionName)
            .add(userInfo)
            .addOnSuccessListener { documentReference ->
                Log.d("REGISTERFIRESTORE :", "SUCCESS added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("REGISTERFIRESTORE :", "Error adding document", e)
            }
    }



    private fun deleteData(userId: String, collectionName: String, onSuccess: () -> Unit) {
        val firestore = Firebase.firestore
        firestore.collection(collectionName)
            .whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    // 찾은 문서를 삭제
                    firestore.collection(collectionName)
                        .document(document.id)
                        .delete()
                        .addOnCompleteListener {
                            Log.d("REGISTERFIRESTORE : ", "DELETE SUCCESS")
                            // 삭제 완료 시 onSuccess 호출
                            onSuccess()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("REGISTERFIRESTORE : ", "Error deleting documents.", exception)
            }
    }

}