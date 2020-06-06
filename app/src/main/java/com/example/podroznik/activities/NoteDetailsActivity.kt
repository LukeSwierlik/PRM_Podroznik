package com.example.podroznik.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.podroznik.R
import com.example.podroznik.models.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_note_details.*
import java.util.*

class NoteDetailsActivity : AppCompatActivity() {

    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.SAVE_BUTTON) {
            val name = NAME_EDIT_TEXT_DETAILS.text.toString()
            val description = DESCRIPTION_EDIT_TEXT_DETAILS.text.toString()
            val diameterCircle = DIAMETER_EDIT_TEXT_DETAILS.text.toString().toDouble()
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val noteId = "${Date().time}";

            val firebaseInput = Note(name, description, diameterCircle, userId, noteId)

            val firebase = FirebaseDatabase.getInstance()
            myRef = firebase.getReference("Notes")

            myRef.child(noteId).setValue(firebaseInput)
        }

        return super.onOptionsItemSelected(item)
    }
}
