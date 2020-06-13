package com.example.podroznik.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.podroznik.R
import com.example.podroznik.adapters.CardViewAdapter
import com.example.podroznik.models.Note
import com.example.podroznik.objects.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_notes.*

class NotesActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    private lateinit var listOfNotes: ArrayList<Note>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        initFirebase()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listOfNotes = ArrayList()

                for (i in dataSnapshot.children) {
                    val newRow = i.getValue(Note::class.java)

                    if (newRow!!.userId == firebaseUser.uid) {
                        val note = Note();

                        note.name = newRow.name
                        note.description = newRow.description
                        note.diameterCircle = newRow.diameterCircle

                        listOfNotes.add(newRow)
                    }
                }

                if (listOfNotes.size > 0) {
                    EMPTY_STATE_CONTAINER.visibility = View.INVISIBLE
                    LIST_CONTAINER.visibility = View.VISIBLE

                    setupAdapter(listOfNotes)
                } else {
                    EMPTY_STATE_CONTAINER.visibility = View.VISIBLE
                    LIST_CONTAINER.visibility = View.INVISIBLE
                }

                NOTES_PROGRESS_BAR.visibility = View.INVISIBLE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)
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

    private fun setupAdapter(arrayData: ArrayList<Note>) {
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.adapter = CardViewAdapter(arrayData)
    }

    private fun initFirebase() {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val firebase = FirebaseDatabase.getInstance()
        databaseReference = firebase.getReference(Config.TABLE_NOTES)
    }
}
