package com.example.podroznik.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.podroznik.R
import com.example.podroznik.objects.Config
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initFirebase()

        CREATE_ACCOUNT_BTN.setOnClickListener {
            val username: String = REGISTER_USERNAME_MET.text.toString()
            val email: String = REGISTER_EMAIL_MET.text.toString()
            val password: String = REGISTER_PASSWORD_MET.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast
                    .makeText(this, "All fields are required", Toast.LENGTH_SHORT)
                    .show()
            } else if (password.length < 6) {
                Toast
                    .makeText(this, "Password must be least 6 characters", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast
                    .makeText(this, "New user created", Toast.LENGTH_SHORT)
                    .show()

                createAccount(username, email, password)
            }
        }
    }

    private fun createAccount(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(OnCompleteListener<AuthResult>() { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser? = auth.currentUser
                    val userId: String = firebaseUser!!.uid

                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap["id"] = userId
                    hashMap["username"] = username

                    databaseReference
                        .child(userId)
                        .setValue(hashMap)
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

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()

        databaseReference = FirebaseDatabase
            .getInstance()
            .getReference(Config.TABLE_USERS)
    }
}
