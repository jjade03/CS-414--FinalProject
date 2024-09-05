package com.example.hw4

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.firebase.ui.auth.IdpResponse

class LoginActivity : AppCompatActivity() {
private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        userLogIn()
    }

    private fun userLogIn() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if(currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val signInActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    //val user = FirebaseAuth.getInstance().currentUser
                    startActivity(intent)
                    finish()
                } else {
                    val response = IdpResponse.fromResultIntent(result.data)
                    if (response == null) {
                        Log.d(TAG, "Sign in request cancelled")
                    } else {
                        Log.d(TAG, "result: ${response.error?.errorCode}")
                    }
                }
            }

            findViewById<Button>(R.id.login_button).setOnClickListener {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build()
                )

                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)
                    .build()
                signInActivity.launch(signInIntent)
            }
        }
    }
}