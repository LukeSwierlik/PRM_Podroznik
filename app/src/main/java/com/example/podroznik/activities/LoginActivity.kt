package com.example.podroznik.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBar
import com.example.podroznik.R
import com.google.firebase.auth.FirebaseAuth
import com.rengwuxian.materialedittext.MaterialEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var email: MaterialEditText
    private lateinit var password: MaterialEditText

    private lateinit var btnLogin: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar);
//        toolbar.title = "Login"

//        val actionbar: ActionBar? = supportActionBar
//        actionbar!!.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        btnLogin = findViewById(R.id.BTN_LOGIN)

        btnLogin.setOnClickListener {
            val txtEmail: String = email.text.toString()
            val txtPassword: String = password.text.toString()

            if (txtEmail.isEmpty() || txtPassword.isEmpty()) {
                Toast
                    .makeText(this, "All fields are required", Toast.LENGTH_SHORT)
                    .show()
            } else {
                auth
                    .signInWithEmailAndPassword(txtEmail, txtPassword)
                    .addOnCompleteListener { task ->
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
