package com.example.podroznik.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.podroznik.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

private val TAG = "TAG"
class MainActivity : AppCompatActivity() {

    private lateinit var login: Button
    private lateinit var register: Button
    private var firebaseUser: FirebaseUser? = null

    override fun onStart() {
        super.onStart()

//        firebaseUser = FirebaseAuth.getInstance().currentUser
//
//        Log.d(TAG, firebaseUser?.uid)
//
//        if (firebaseUser != null) {
//            val intent = Intent(this, NotesActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        login = findViewById(R.id.login)
        register = findViewById(R.id.register)

        login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
