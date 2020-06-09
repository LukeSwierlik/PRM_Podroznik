package com.example.podroznik.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.podroznik.R
import com.example.podroznik.models.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_note_details.*
import java.io.IOException
import java.util.*


class NoteDetailsActivity : AppCompatActivity() {

    private lateinit var myRef: DatabaseReference

    private var filePath: Uri? = null

    private val PICK_IMAGE_REQUEST = 71

    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)

        storage = FirebaseStorage.getInstance();
        storageReference = storage!!.reference;

        btnChoose.setOnClickListener { chooseImage() }

        btnUpload.setOnClickListener { uploadImage() }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == Activity.RESULT_OK &&
            data != null && data.data != null) {
            filePath = data.data

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)

                imgView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    private fun uploadImage() {
        if (filePath != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val ref =
                storageReference!!.child("images/" + UUID.randomUUID().toString())

            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    progressDialog.dismiss()

                    val name = NAME_EDIT_TEXT_DETAILS.text.toString()
                    val description = DESCRIPTION_EDIT_TEXT_DETAILS.text.toString()
                    val diameterCircle = DIAMETER_EDIT_TEXT_DETAILS.text.toString().toDouble()
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid
                    val noteId = "${Date().time}";

                    ref.downloadUrl.addOnCompleteListener () {taskSnapshot ->
                        val url = taskSnapshot.result
                        val imageURL = url.toString()

                        val firebaseInput = Note(name, description, diameterCircle, userId, noteId, imageURL)

                        val firebase = FirebaseDatabase.getInstance()
                        myRef = firebase.getReference("Notes")

                        myRef.child(noteId).setValue(firebaseInput)

                        Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                            .totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                }
        }
    }
}
