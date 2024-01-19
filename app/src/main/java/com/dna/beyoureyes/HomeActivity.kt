package com.dna.beyoureyes

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.dna.beyoureyes.databinding.ActivityAlertDialogDefaultBinding
import com.dna.beyoureyes.databinding.ActivityHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.opencv.android.OpenCVLoader


class HomeActivity : AppCompatActivity() {

    // onBackPressed
    private var time: Long = 0
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)

        //openCV
        OpenCVLoader.initDebug()

        Log.d("HOMEFIRESTORE : ", "success0")

        // 화면 크기 가져오기
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        // 원하는 비율에 따라 padding 조절
        val motionLayout: MotionLayout = binding.homeMotionLayout
        val desiredPadding = calculateDesiredPadding(screenHeight)
        motionLayout.setPadding(48, desiredPadding, 48, desiredPadding)


        if (AppUser.id != null) {
            // 안드로이드 파이어베이스 - 파이어 스토어에 임의의 정보 저장
            val db = Firebase.firestore
            // 유저 정보 받아오기 - userId가 일치하는 경우에만!!
            db.collection("userInfo")
                .whereEqualTo("userID", AppUser.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        // 유저 정보가 이미 존재하는 경우
                        if (result != null && !result.isEmpty) {
                            for (document in result) {
                                Log.d("FIRESTORE : ", "${document.id} => ${document.data}")

                                // Firebase 문서에서 사용자 정보 파싱하여 UserInfo 객체 생성
                                val user = UserInfo.parseFirebaseDoc(document)

                                // 싱글톤 객체 유저 정보 업뎃
                                if (user != null) {
                                    AppUser.info = user
                                    break
                                }
                            }
                        } // 싱글톤 객체 사용으로 불필요해진 else 절 삭제
                    } else {
                        // 쿼리 중에 예외가 발생한 경우
                        // 쿼리 실패의 경우 인터넷 연결 상태와도 연관이 있으므로
                        // 추후 대응 필요성을 고려해 else문 분기 유지
                        Log.d("HOMEFIRESTORE : ", "Error getting documents.", task.exception)
                    }
                }
        } else { // 사용자 id 자체가 없는 경우? 오류
            Log.d("HOMEFIRESTORE : ", "Error getting documents.")
            Log.d("HOME : ", AppUser.id!!)
            Toast.makeText(this@HomeActivity, "userId not exist", Toast.LENGTH_LONG).show()
        }

        // 4개의 메뉴 버튼 레이아웃 연결
        val filmButton: ImageView = binding.filmButton
        val todayIntakeButton: ImageView = binding.todayNutritionButton
        val exitButton: ImageView = binding.exitButton
        val userInfoButton: ImageView = binding.myProfileButton

        // 내 질환정보 수정하기 클릭 시...정보가 없으면 정보 등록 페이지로 넘어가도록 함
        userInfoButton.setOnClickListener {
            if (AppUser.info != null) {   // userInfo가 있는 경우
                val intent = Intent(this, UserInfoActivity::class.java)
                Log.d("HOMEFIRESTORE : ", "success_1")
                //Toast.makeText(this@HomeActivity, "TRUE", Toast.LENGTH_LONG).show()
                startActivity(intent)
            } else { //userInfo가 없는 경우
                val intent = Intent(this, UserInfoRegisterActivity::class.java)
                Log.d("HOMEFIRESTORE : ", "success_-1")
                //Toast.makeText(this@HomeActivity, "FALSE", Toast.LENGTH_LONG).show()
                startActivity(intent)
            }
        } // 사용자정보존재, 존재하지않음 2가지로만 분기 축소

        // 촬영하기 버튼
        filmButton.setOnClickListener {
            // val intent = Intent(this, CameraFirstActivity::class.java)
            val intent = Intent(this, CameraFirstActivity::class.java)
            startActivity(intent)
        }
        // 오늘 섭취한 영양소 확인하기 버튼
        todayIntakeButton.setOnClickListener {
            if (AppUser.info != null) { // 사용자 정보 있을 시 맞춤 섭취량 통계 화면으로 연결
                // 이제 사용자 정보 인텐트 파라미터로 전달할 필요X
                val intent = Intent(this, TodayIntakePersonalizedActivity::class.java)
                startActivity(intent)
            } else {// 사용자 정보 없을 시 일반 통계 화면으로 연결
                val intent = Intent(this, TodayIntakeActivity::class.java)
                startActivity(intent)
            }
        }
        // 나가기 버튼
        exitButton.setOnClickListener {
            val dialogView =
                LayoutInflater.from(this).inflate(R.layout.activity_alert_dialog_default, null)
            val alertDialogBuilder = AlertDialog.Builder(this)
            val alertDialogBinding = ActivityAlertDialogDefaultBinding.inflate(layoutInflater)
            alertDialogBuilder.setView(alertDialogBinding.root)

            val alertDialog = alertDialogBuilder.create()

            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            alertDialogBinding.rightBtn.setOnClickListener {
                try {
                    // finish 후 다른 Activity 뜨지 않도록 함
                    moveTaskToBack(true)
                    // 현재 액티비티 종료
                    finish()
                    // 모든 루트 액티비티 종료
                    finishAffinity()
                    // 인텐트 애니메이션 종료
                    overridePendingTransition(0, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                alertDialog.dismiss()
            }

            alertDialogBinding.leftBtn.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialogBinding.title.text = "어플리케이션 종료"
            alertDialogBinding.text.text = "어플리케이션을 종료하시겠어요?"

            alertDialog.show()
        }


    }
    // 화면의 비율 구하기
    private fun calculateDesiredPadding(screenHeight: Int): Int {
        // 원하는 비율에 따라 계산된 padding 값을 반환
        // 예: 세로 길이의 10%를 padding으로 사용하려면 screenHeight * 0.1을 반환
        return (screenHeight * 0.1).toInt()
    }


    override fun onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis()

            val dialogView =
                LayoutInflater.from(this).inflate(R.layout.activity_alert_dialog_default, null)

            val alertDialogBuilder = AlertDialog.Builder(this)
            val alertDialogBinding = ActivityAlertDialogDefaultBinding.inflate(layoutInflater)
            alertDialogBuilder.setView(alertDialogBinding.root)

            val alertDialog = alertDialogBuilder.create()

            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            alertDialogBinding.rightBtn.setOnClickListener {
                try {
                    // finish 후 다른 Activity 뜨지 않도록 함
                    moveTaskToBack(true)
                    // 현재 액티비티 종료
                    finish()
                    // 모든 루트 액티비티 종료
                    finishAffinity()
                    // 인텐트 애니메이션 종료
                    overridePendingTransition(0, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                alertDialog.dismiss()
            }

            alertDialogBinding.leftBtn.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialogBinding.title.text = "어플리케이션 종료"
            alertDialogBinding.text.text = "어플리케이션을\n종료하시겠어요?"

            alertDialog.show()
        }
    }


}