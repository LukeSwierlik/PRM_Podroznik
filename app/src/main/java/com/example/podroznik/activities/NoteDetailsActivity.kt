package com.example.podroznik.activities

import android.app.Activity
import android.app.ProgressDialog
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
import com.example.podroznik.objects.Config
import com.example.podroznik.objects.Fields
import com.example.podroznik.utils.GlideApp
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

    private var databaseReference: DatabaseReference? = null
    private var filePath: Uri? = null
    private var storageReference: StorageReference? = null
    private var mCurrentPhotoPath: String? = null;

    private var isChooseNewImage = false

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val PICK_IMAGE_REQUEST = 71
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)

        initFirebase()
        viewExtraParamsInActivity()
        listenerButtons()
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

        if (requestCode == REQUEST_IMAGE_CAPTURE &&
            resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
            isChooseNewImage = true

            IMAGE_VIEW.setImageBitmap(bitmap)
        }

        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == Activity.RESULT_OK &&
            data != null && data.data != null) {
            filePath = data.data

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)

                isChooseNewImage = true

                IMAGE_VIEW.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun onClickCamera(view: View) {
        openCamera()
    }

    private fun updateNotes() {
        val noteId = intent.getStringExtra(Fields.ID)
        val imageURL = intent.getStringExtra(Fields.IMAGE_URL)

        if (isChooseNewImage) {
            // kiedy robisz edycje
            uploadImage(noteId)
        } else if (noteId != null && imageURL != null) {
            // kiedy zapisujesz wraz ze zdjęciem
            save(noteId, imageURL)
        } else {
            // kiedy zapisujesz bez zdjęcia
            save(null, "")
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            Config.AUTHORITY_FILE_PROVIDER,
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        filePath = uri
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
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

            val ref = storageReference!!.child(Config.STORAGE_IMAGES + UUID.randomUUID().toString())

            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    progressDialog.dismiss()

                    ref.downloadUrl.addOnCompleteListener () { taskSnapshot ->
                        val url = taskSnapshot.result
                        val imageURL = url.toString()

                        save(noteUuid, imageURL)
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

    private fun save(noteUuid: String?, imageURL: String = "") {
        val name = NAME_EDIT_TEXT.text.toString()
        val description = DESCRIPTION_EDIT_TEXT.text.toString()
        val diameterCircle = DIAMETER_EDIT_TEXT.text.toString().toDouble()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        var noteId = "${Date().time}";

        if (noteUuid != null) {
            noteId = noteUuid
        }

        val firebaseInput = Note(name, description, diameterCircle, userId, noteId, imageURL)

        databaseReference!!.child(noteId).setValue(firebaseInput)

        Toast
            .makeText(this, "Saved", Toast.LENGTH_SHORT)
            .show()
    }

    private fun viewExtraParamsInActivity() {
        if (intent.hasExtra(Fields.NAME) &&
            intent.hasExtra(Fields.DESCRIPTION) &&
            intent.hasExtra(Fields.DIAMETER_CIRCLE) &&
            intent.hasExtra(Fields.IMAGE_URL)) {

            NAME_EDIT_TEXT.setText(intent.getStringExtra(Fields.NAME))
            DESCRIPTION_EDIT_TEXT.setText(intent.getStringExtra(Fields.DESCRIPTION))
            DIAMETER_EDIT_TEXT.setText(intent.getStringExtra(Fields.DIAMETER_CIRCLE))

            val imageURL = intent.getStringExtra(Fields.IMAGE_URL)

            if (imageURL.isNotEmpty()) {
                val storage = FirebaseStorage.getInstance();
                val gsReference = storage.getReferenceFromUrl(imageURL)

                GlideApp.with(this)
                    .load(gsReference)
                    .into(IMAGE_VIEW)
            }
        }
    }

    private fun listenerButtons() {
        PHOTO_ACTION_BUTTON.setOnClickListener { chooseImage() }
    }

    private fun initFirebase() {
        val storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        val firebase = FirebaseDatabase.getInstance()
        databaseReference = firebase.getReference(Config.TABLE_NOTES)
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
}
