package com.example.podroznik.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.podroznik.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var username: MaterialEditText
    private lateinit var email: MaterialEditText
    private lateinit var password: MaterialEditText

    private lateinit var btnRegister: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var refDB: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        toolbar.title = "Register"
//        setSupportActionBar(toolbar);
//
//        val actionbar: ActionBar? = supportActionBar
//        actionbar!!.setDisplayHomeAsUpEnabled(true)

        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        btnRegister = findViewById(R.id.BTN_REGISTER)

        auth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener {
            val txtUsername: String = username.text.toString()
            val txtEmail: String = email.text.toString()
            val txtPassword: String = password.text.toString()

            if (txtUsername.isEmpty() || txtEmail.isEmpty() || txtPassword.isEmpty()) {
                Toast
                    .makeText(this, "All fields are required", Toast.LENGTH_SHORT)
                    .show()
            } else if (txtPassword.length < 3) {
                Toast
                    .makeText(this, "Password must be least 3 characters", Toast.LENGTH_SHORT)
                    .show()
            } else {
                register(txtUsername, txtEmail, txtPassword)
            }
        }
    }

    private fun register(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(OnCompleteListener<AuthResult>() { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser? = auth.currentUser
                    val userId: String = firebaseUser!!.uid

                    refDB = FirebaseDatabase
                        .getInstance()
                        .getReference("Users")
                        .child(userId)

                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap["id"] = userId
                    hashMap["username"] = username

                    refDB.setValue(hashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this, MainActivity::class.java )
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)

                                finish()
                            }
                        }
                } else {
                    Toast
                        .makeText(this, "You can't register both email and password", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}
