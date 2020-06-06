package com.example.podroznik.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.example.podroznik.R
import com.example.podroznik.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

private val TAG = "TAG"
class NotesActivity : AppCompatActivity() {

    private lateinit var username: TextView
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

//        username = findViewById(R.id.username)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        dbRef = FirebaseDatabase
            .getInstance()
            .getReference("Users")
            .child(firebaseUser.uid)

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User = snapshot.getValue(User::class.java) as User
//                username.text = user.username
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "====== jest menu")
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()

                return true
            }
        }

        return false
    }

    fun onClickNoteDetails(view: View) {
        val intent = Intent(applicationContext, NoteDetailsActivity::class.java)
        startActivity(intent)
    }
}
