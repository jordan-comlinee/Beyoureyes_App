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
import android.widget.ImageButton
import android.widget.TextView
import java.io.Serializable


val diseaseList : List<String> = listOf("고혈압", "고지혈증", "당뇨")
val allergyList : List<String> = listOf("메밀", "밀", "콩", "호두", "땅콩", "복숭아", "토마토", "돼지고기", "난류", "우유", "닭고기", "쇠고기", "새우", "고등어", "홍합", "전복", "굴", "조개류", "게", "오징어", "아황산")

class UserInfoRegisterActivity : AppCompatActivity() {
    private var clickedDisease : MutableList<Boolean> = mutableListOf(false, false, false)
    private var clickedAllergic : MutableList<Boolean> = MutableList(21) { false }
    private val userDiseaseList : ArrayList<String> = arrayListOf()
    private val userAllergyList : ArrayList<String> = arrayListOf()

    private var userInfoCheck : Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info_register)
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)

        //val userInfoRegisterTitle = findViewById<TextView>(R.id.userInfoRegisterTitle)

        val age : EditText = findViewById(R.id.editAge)

        val chip0 = findViewById<Chip>(R.id.chip0)
        val chip1 = findViewById<Chip>(R.id.chip1)
        val chip2 = findViewById<Chip>(R.id.chip2)

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


        val userIdClass: userId = application as userId
        val userId = userIdClass.userId

        // 안드로이드 파이어베이스 - 파이어 스토어에 임의의 정보 저장
        val db = Firebase.firestore
        // 유저 정보 받아오기 - userId가 일치하는 경우에만!!
        db.collection("userInfo")
            .whereEqualTo("userID", userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    // 유저 정보가 이미 존재하는 경우
                    if (result != null && !result.isEmpty) {
                        Log.d("REGISTERFIRESTORE : ", "getDataSuccess_exist")
                        toolbarTitle.setText("내 정보 수정하기")
                        //userInfoRegisterTitle.setText("내 정보 수정하기")
                        userInfoCheck = 1
                    }
                    // 유저 정보가 존재하지 않는 경우
                    else {
                        Log.d("REGISTERFIRESTORE : ", "getDataSuccess_not exist")
                        toolbarTitle.setText("내 정보 등록하기")
                        userInfoCheck = -1
                    }
                } else {
                    // 쿼리 중에 예외가 발생한 경우
                    Log.d("REGISTERFIRESTORE : ", "Error getting documents.", task.exception)
                    toolbarTitle.setText("내 정보 등록하기")
                    userInfoCheck = 0
                }
            }


        // 질환 정보 클릭 로직
        for (i in diseaseChips.indices) {
            diseaseChips[i].setOnClickListener { view ->
                val chip = view as Chip

                //val chipText = chip.text.toString()
                // Toast 메시지 표시
                //Toast.makeText(this@UserInfoRegisterActivity, chipText, Toast.LENGTH_LONG).show()

                // clickedDisease 리스트 업데이트
                val index = diseaseChips.indexOf(chip)
                clickedDisease[index] = !clickedDisease[index]

                // clickedDisease의 값을 확인하여 배경색과 텍스트 색상 변경
                if (clickedDisease[index]) {
                    // 클릭되었을 때의 처리
                    chip.setChipBackgroundColorResource(R.color.red)
                    chip.setTextColor(Color.WHITE)
                } else {
                    // 클릭이 해제되었을 때의 처리
                    // 원래의 배경색과 텍스트 색상을 복원하려면 해당 색상 리소스 ID를 사용하세요.
                    chip.setChipBackgroundColorResource(R.color.white)
                    chip.setTextColor(Color.BLACK) // 원래의 텍스트 색상을 복원
                }
            }
        }
        // 알러지 정보 클릭 로직
        for (i in allergyChips.indices) {
            allergyChips[i].setOnClickListener { view ->
                val chip = view as Chip

                //val chipText = chip.text.toString()
                //Toast.makeText(this@UserInfoRegisterActivity, chipText, Toast.LENGTH_LONG).show()


                // clickedDisease 리스트 업데이트
                val index = allergyChips.indexOf(chip)
                clickedAllergic[index] = !clickedAllergic[index]

                // clickedDisease의 값을 확인하여 배경색과 텍스트 색상 변경
                if (clickedAllergic[index]) {
                    // 클릭되었을 때의 처리
                    chip.setChipBackgroundColorResource(R.color.red)
                    chip.setTextColor(Color.WHITE)
                } else {
                    // 클릭이 해제되었을 때의 처리
                    // 원래의 배경색과 텍스트 색상을 복원하려면 해당 색상 리소스 ID를 사용하세요.
                    chip.setChipBackgroundColorResource(R.color.white)
                    chip.setTextColor(Color.BLACK) // 원래의 텍스트 색상을 복원
                }
            }
        }

        // 등록하기 버튼 클릭 시 로직
        usrInfoRegiSaveButton.setOnClickListener {
            // 나이 입력 X 시 토스트 메세지 띄움
            if(age.text.toString() == ""){
                Toast.makeText(this@UserInfoRegisterActivity, "나이를 입력해주세요!", Toast.LENGTH_LONG).show()
            }
            // 나이 입력 O 시
            else {
                // 질환 정보
                for( index in clickedDisease.indices ) {
                    if(clickedDisease[index])
                        userDiseaseList.add(diseaseList[index])
                }
                // 알러지 정보
                for ( index in clickedAllergic.indices ) {
                    if(clickedAllergic[index])
                        userAllergyList.add(allergyList[index])
                }

                Log.d("LIST: ", userDiseaseList.toString())
                Log.d("LIST: ", userAllergyList.toString())

                // 기존 유저 정보가 있다면 삭제
                if(userInfoCheck == 1) {
                    Log.d("REGISTERFIRESTORE : ", "DELETE START")
                    deleteData(userId!!, "userInfo") {
                        // 삭제가 완료되면 이 블록이 실행됨
                        // 여기에서 sendData 함수 호출
                        val userInfo = hashMapOf(
                            "userID" to userId,
                            "userAge" to age.text.toString().toInt(),
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
                    // 유저 정보가 없는 경우에는 바로 sendData 함수 호출
                    val userInfo = hashMapOf(
                        "userID" to userId!!,
                        "userAge" to age.text.toString().toInt(),
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