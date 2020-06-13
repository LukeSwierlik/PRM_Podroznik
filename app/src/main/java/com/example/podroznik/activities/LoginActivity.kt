package com.example.podroznik.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.podroznik.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        SIGN_IN_BTN.setOnClickListener {
            val txtEmail: String = LOGIN_EMAIL_MET.text.toString()
            val txtPassword: String = LOGIN_PASSWORD_MET.text.toString()

            if (txtEmail.isEmpty() || txtPassword.isEmpty()) {
                Toast
                    .makeText(this, "All fields are required", Toast.LENGTH_SHORT)
                    .show()
            } else {
                auth
                    .signInWithEmailAndPassword(txtEmail, txtPassword)
                    .addOnCompleteListener { task ->
                        LOGIN_PROGRESS_BAR.visibility = View.VISIBLE
                        LOGIN_LINEAR_LAYOUT.visibility = View.INVISIBLE

                        if (task.isSuccessful) {
                            val intent = Intent(this, NotesActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

                            startActivity(intent)
                            finish()
                        } else {
                            Toast
                                .makeText(this, "Authentication failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }
    }
}
