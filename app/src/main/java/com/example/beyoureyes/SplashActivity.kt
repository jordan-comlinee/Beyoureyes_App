package com.example.beyoureyes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    private lateinit var userId : String
    // [END declare_auth]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Toast.makeText( this@SplashActivity, "Authentication start", Toast.LENGTH_SHORT).show()
        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth

        // [END initialize_auth]
    }

    // [START on_start_check_user]
    public override fun onStart() {

        //Toast.makeText( this@SplashActivity, "Authentication onStart", Toast.LENGTH_SHORT).show()
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser

        if (currentUser != null) {
            //Toast.makeText(this@SplashActivity, "이미 가입한 유저", Toast.LENGTH_LONG).show()
            userId = currentUser.uid
            val userIdClass = application as userId
            userIdClass.userId = userId
            Log.d("USERID : ", userId)
            //Toast.makeText(this@SplashActivity, userId, Toast.LENGTH_LONG).show()
            Handler().postDelayed({ startActivity(Intent(this, HomeActivity::class.java)) }, 3 * 1000)
        }
        else {
            //Toast.makeText(this@SplashActivity, "가입안한 유저", Toast.LENGTH_LONG).show()
            signInAnonymously()
            Handler().postDelayed({ startActivity(Intent(this, HomeActivity::class.java)) }, 3 * 1000)
        }
        updateUI(currentUser)

    }
    // [END on_start_check_user]

    private fun signInAnonymously() {
        // [START signin_anonymously]
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Toast.makeText(this@SplashActivity, "Authentication successed.", Toast.LENGTH_SHORT).show()
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SIGN", "signInAnonymously:success")
                    val user = auth.currentUser
                    // 과연 절대로 null이 아닐까?
                    userId = user!!.uid
                    val userIdClass = application as userId
                    userIdClass.userId = userId
                    Log.d("USERID : ", userId)
                    //Toast.makeText(this@SplashActivity, userId, Toast.LENGTH_LONG).show()
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SIGN", "signInAnonymously:failure", task.exception)
                    //Toast.makeText( this@SplashActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
        // [END signin_anonymously]
    }

    private fun linkAccount() {
        // Create EmailAuthCredential with email and password
        val credential = EmailAuthProvider.getCredential("", "")
        // [START link_credential]
        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "linkWithCredential:success")
                    val user = task.result?.user
                    updateUI(user)
                } else {
                    Log.w(TAG, "linkWithCredential:failure", task.exception)
                    // Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
        // [END link_credential]
    }

    private fun updateUI(user: FirebaseUser?) {
    }

    companion object {
        private const val TAG = "AnonymousAuth"
    }


}