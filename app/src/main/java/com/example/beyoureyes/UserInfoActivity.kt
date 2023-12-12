package com.example.beyoureyes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


val diseaseKoreanList : List<String> = listOf("고혈압", "고지혈증", "당뇨")
val allergyKoreanList : List<String> = listOf("메밀", "밀", "콩", "호두", "땅콩", "복숭아", "토마토", "돼지고기", "난류", "우유", "닭고기", "쇠고기", "새우", "고등어", "홍합", "전복", "굴", "조개류", "게", "오징어", "아황산")



class UserInfoActivity : AppCompatActivity() {
    private val userDiseaseList : ArrayList<String> = arrayListOf()
    private val userAllergyList : ArrayList<String> = arrayListOf()

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)

        val diseaseChipGroup = findViewById<ChipGroup> (R.id.diseaseChipGroup)
        val allergicChipGroup  = findViewById<ChipGroup>(R.id.allergyChipGroup)

        val infoAge = findViewById<TextView>(R.id.infoAge)

        val userInfoChangeButton = findViewById<Button>(R.id.userInfoChangeButton)
        val googleConnectButton = findViewById<Button>(R.id.googleConnectButton)

        //toolBar
        val toolBar = findViewById<Toolbar>(R.id.toolbarDefault)
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        val toolbarBackButton = findViewById<ImageButton>(R.id.toolbarBackBtn)
        setSupportActionBar(toolBar)
        //Toolbar에 앱 이름 표시 제거!!
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle.setText("내 질환 확인하기")


        toolbarBackButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            //overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_enter)
        }


        val userIdClass = application as userId
        val userId = userIdClass.userId
        val db = Firebase.firestore
        db.collection("userInfo")
            .whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("FIRESTORE : ", "${document.id} => ${document.data}")
                    // Firestore에서 가져온 나이 정보 입력
                    infoAge.text = document.data.get("userAge").toString() + "세"
                    val userDisease = document.data.get("userDisease") as ArrayList<String>
                    val userAllergic = document.data.get("userAllergic") as ArrayList<String>
                    // Firestore에서 가져온 질환 정보 입력
                    if (userDisease != null) {
                        userDiseaseList.addAll(userDisease)
                        for (diseaseItem in userDiseaseList) {
                            val chip = Chip(this)
                            chip.text = diseaseItem
                            chip.setChipBackgroundColorResource(R.color.red)
                            chip.setTextColor(Color.WHITE)
                            diseaseChipGroup.addView(chip)
                        }
                    }
                    Log.d("FIRESTORE", userDiseaseList.toString())
                    // Firestore에서 가져온 알러지 정보 입력
                    if (userAllergic != null) {
                        userAllergyList.addAll(userAllergic)
                        for (allergyItem in userAllergyList) {
                            val chip = Chip(this)
                            chip.text = allergyItem
                            chip.setChipBackgroundColorResource(R.color.red)
                            chip.setTextColor(Color.WHITE)
                            allergicChipGroup.addView(chip)
                        }
                    }
                    Log.d("FIRESTORE", userAllergic.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FIRESTORE : ", "Error getting documents.", exception)
            }
        // 수정하기 버튼 클릭 시 작용
        userInfoChangeButton.setOnClickListener {
            val intent = Intent(this, UserInfoRegisterActivity::class.java)
            startActivity(intent)
        }
        //구글 계정 연동 버튼 클릭 시 적용
         googleConnectButton.setOnClickListener {

             // GoogleSignInOptions 설정
             val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                 .requestIdToken(getString(R.string.default_web_client_id))
                 .requestEmail()
                 .build()

             // GoogleSignInClient 설정
             mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

             // 구글 연동 버튼 초기화
             val googleConnectButton: Button = findViewById(R.id.googleConnectButton)

             // 구글 연동 버튼 클릭 리스너 설정
             googleConnectButton.setOnClickListener {
                 Log.d("INFO : ", "Google Connect Button Clicked")

                 // 이미 익명 로그인이 성공한 상태에서 구글 로그인 시작
                 startGoogleSignIn()
             }
         }

    }

    private fun startGoogleSignIn() {
        // 구글 로그인 인텐트 설정
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Firebase 인증 객체 초기화
            val auth = FirebaseAuth.getInstance()
            // Firebase에 Google 로그인 정보로 인증
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("INFO : ", "Google 로그인 성공")
                    } else {
                        Log.e("ERROR : ", "Google 로그인 실패", task.exception)
                    }
                }

        } catch (e: ApiException) {
            Log.e("ERROR : ", "Google 로그인 실패", e)
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

}