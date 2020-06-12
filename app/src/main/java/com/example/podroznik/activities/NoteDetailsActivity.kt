package com.example.podroznik.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.podroznik.R
import com.example.podroznik.models.Note
import com.example.podroznik.objects.Fields
import com.example.podroznik.objects.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_note_details.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NoteDetailsActivity : AppCompatActivity() {

    private lateinit var myRef: DatabaseReference

    private var filePath: Uri? = null

    private val PICK_IMAGE_REQUEST = 71

    private var isChooseNewImage = false
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null

    private var mCurrentPhotoPath: String? = null;

    val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)

        viewExtraParamsInActivity()

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
            updateNotes()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
            imgView.setImageBitmap(bitmap)
        }

        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == Activity.RESULT_OK &&
            data != null && data.data != null) {
            filePath = data.data

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)

                isChooseNewImage = true

                imgView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateNotes() {
        val noteId = intent.getStringExtra(Fields.ID)

        if (isChooseNewImage) {
            uploadImage(noteId)
        } else {
            val name = NAME_EDIT_TEXT_DETAILS.text.toString()
            val description = DESCRIPTION_EDIT_TEXT_DETAILS.text.toString()
            val diameterCircle = DIAMETER_EDIT_TEXT_DETAILS.text.toString().toDouble()
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val imageURL = intent.getStringExtra(Fields.IMAGE_URL)

            val firebaseInput = Note(name, description, diameterCircle, userId, noteId, imageURL)

            val firebase = FirebaseDatabase.getInstance()
            myRef = firebase.getReference("Notes")

            myRef.child(noteId).setValue(firebaseInput)

            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickCamera(view: View) {
        openCamera()
    }

    private fun openCamera() {
        val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "com.example.podroznik.fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    private fun uploadImage(noteUuid: String? = null) {
        if (filePath != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val ref =
                storageReference!!.child("images/" + UUID.randomUUID().toString())

            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    progressDialog.dismiss()

                    ref.downloadUrl.addOnCompleteListener () {taskSnapshot ->
                        val url = taskSnapshot.result
                        val imageURL = url.toString()

                        val name = NAME_EDIT_TEXT_DETAILS.text.toString()
                        val description = DESCRIPTION_EDIT_TEXT_DETAILS.text.toString()
                        val diameterCircle = DIAMETER_EDIT_TEXT_DETAILS.text.toString().toDouble()
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                        var noteId = "${Date().time}";

                        if (noteUuid != null) {
                            Log.d("TAG", "podmianka")
                            noteId = noteUuid
                        }

                        val firebaseInput = Note(name, description, diameterCircle, userId, noteId, imageURL)

                        val firebase = FirebaseDatabase.getInstance()
                        myRef = firebase.getReference("Notes")

                        Log.d("TAG", firebaseInput.toString())

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

    private fun viewExtraParamsInActivity() {
        if (intent.hasExtra(Fields.NAME) &&
            intent.hasExtra(Fields.DESCRIPTION) &&
            intent.hasExtra(Fields.DIAMETER_CIRCLE) &&
            intent.hasExtra(Fields.IMAGE_URL)) {

            NAME_EDIT_TEXT_DETAILS.setText(intent.getStringExtra(Fields.NAME))
            DESCRIPTION_EDIT_TEXT_DETAILS.setText(intent.getStringExtra(Fields.DESCRIPTION))
            DIAMETER_EDIT_TEXT_DETAILS.setText(intent.getStringExtra(Fields.DIAMETER_CIRCLE))

            val imageURL = intent.getStringExtra(Fields.IMAGE_URL)
            val storage = FirebaseStorage.getInstance();
            val gsReference = storage.getReferenceFromUrl(imageURL)

            GlideApp.with(this)
                .load(gsReference)
                .into(imgView)
        }
    }
}
