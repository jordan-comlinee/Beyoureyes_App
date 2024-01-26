package com.dna.beyoureyes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.dna.beyoureyes.databinding.ActivityUserInfoBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

val diseaseKoreanList : List<String> = listOf("고혈압", "고지혈증", "당뇨")
val allergyKoreanList : List<String> = listOf("메밀", "밀", "콩", "호두", "땅콩", "복숭아", "토마토", "돼지고기", "난류", "우유", "닭고기", "쇠고기", "새우", "고등어", "홍합", "전복", "굴", "조개류", "게", "오징어", "아황산")

class UserInfoActivity : AppCompatActivity() {
    private val userDiseaseList : ArrayList<String> = arrayListOf()
    private val userAllergyList : ArrayList<String> = arrayListOf()

    //google login을 위한 동작
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var oneTapClient: SignInClient

    private lateinit var auth: FirebaseAuth
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var signInRequest: BeginSignInRequest

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true
    private lateinit var binding: ActivityUserInfoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)    // 화면 전환 시 애니메이션
        Log.d(TAG, AppUser.id.toString()+"   AGAIN")
        auth = Firebase.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        oneTapClient = Identity.getSignInClient(this)

        val diseaseChipGroup = binding.diseaseChipGroup
        val allergicChipGroup  = binding.allergyChipGroup
        var sex : Int = 2

        val infoAge = binding.infoAge
        val infoSex = binding.infoSex

        val userInfoChangeButton = binding.userInfoChangeButton
        val googleConnectButton = binding.googleConnectButton
        
        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "내 질환 확인하기"

        binding.include.toolbarBackBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // 사용자 정보 화면 표시 ---------------------------------------------
        AppUser.info?.let {
            // 나이 정보 표시
            infoAge.text = it.age.toString() + "세"
            // 성별 정보 표시
            when(it.gender) {
                0 -> infoSex.setText("여성")
                1 -> infoSex.setText("남성")
                2 -> infoSex.setText("정보가 없습니다. 추가해주세요!")
            }
            // 질환 정보 표시
            it.disease?.let { diseaseArray ->
                for (diseaseItem in diseaseArray) {
                    val chip = Chip(this)
                    chip.text = diseaseItem
                    chip.setChipBackgroundColorResource(R.color.red)
                    chip.setTextColor(Color.WHITE)
                    diseaseChipGroup.addView(chip)
                }

            }
            // 알러지 정보 표시
            it.allergic?.let { allergyArray ->
                for (allergyItem in allergyArray ) {
                    val chip = Chip(this)
                    chip.text = allergyItem
                    chip.setChipBackgroundColorResource(R.color.red)
                    chip.setTextColor(Color.WHITE)
                    allergicChipGroup.addView(chip)
                }
            }
        }?:run{ // 사용자 정보 null일 시 -> 처리 조건 상 이 분기는 아마 진입할 일이 없긴 할 것
            // 나이 정보 표시
            infoAge.text = "나이 정보 확인 실패"
            // 성별 정보 표시
            infoSex.text = "성별 정보 확인 실패"
        }


        // 수정하기 버튼 클릭 시 작용
        userInfoChangeButton.setOnClickListener {
            val intent = Intent(this, UserInfoRegisterActivity::class.java)
            startActivity(intent)
        }
        // Google 로그인 버튼 클릭 이벤트 처리
        googleConnectButton.setOnClickListener {
            // 로그인 요청
            Log.d("GOOGLE : ", "이벤트 시작")
            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.your_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build()
            Log.d("GOOGLE : ", "이벤트 수행중?")
            signIn()
        }

    } // onCreate
    // [START on_start_check_user]
    override fun onBackPressed() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        Log.d("GOOGLE : ", "onStart")
        updateUI(currentUser)
    }

    // 로그인 결과 처리 메서드
    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("GOOGLE : ", "onActivityResult")
        val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
        val idToken = googleCredential.googleIdToken
        when {
            idToken != null -> {
                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("GOOGLE : ", "signInWithCredential:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("GOOGLE : ", "signInWithCredential:failure", task.exception)
                            updateUI(null)
                        }
                    }
            }

            else -> {
                // Shouldn't happen.
                Log.d("GOOGLE : ", "No ID token!")
            }
        }
    } // onActivityResult

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("GOOGLE : ", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("GOOGLE : ", "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }
    // [END auth_with_google]

    // [START signin]
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // [END signin]

    companion object {
        private const val RC_SIGN_IN = 9001
    }
    */

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        } else {
            Log.d(TAG, "onActivityResult: resultCode = $resultCode")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this@UserInfoActivity, "성공!", Toast.LENGTH_LONG).show()
                    val user = auth.currentUser
                    updateUI(user)
                    AppUser.id = user!!.uid
                    Log.d(TAG, AppUser.id.toString())
                    val intent = intent
                    finish()
                    startActivity(intent)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    fun updateUI(user: FirebaseUser?) {

    }
    companion object {
        private const val TAG = "GOOGLE : "
        private const val RC_SIGN_IN = 9001
    }



}