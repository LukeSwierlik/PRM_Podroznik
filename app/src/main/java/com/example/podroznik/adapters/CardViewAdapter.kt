package com.example.podroznik.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.podroznik.R
import com.example.podroznik.activities.NoteDetailsActivity
import com.example.podroznik.models.Note
import com.example.podroznik.objects.Fields
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_card_view.view.*

class CardViewAdapter(
    private val mNotes: ArrayList<Note>
): RecyclerView.Adapter<CardViewAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.activity_card_view, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNotes.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val context = holder.itemView.context
        val idEdit = mNotes[holder.adapterPosition].noteId.toString()
        val imageURLEdit = mNotes[holder.adapterPosition].imageURL

        val noteCardView = holder.itemView.NOTE_CARD_VIEW
        val name = holder.itemView.NAME_TEXT_VIEW_CARD
        val description = holder.itemView.DESCRIPTION_TEXT_VIEW_CARD
        val diameterCircle = holder.itemView.DIAMETER_CIRCLE

        name.text = mNotes[holder.adapterPosition].name
        description.text = mNotes[holder.adapterPosition].description
        diameterCircle.text = mNotes[holder.adapterPosition].diameterCircle.toString()

        noteCardView.setOnClickListener {
            val intentEdit = Intent(context, NoteDetailsActivity::class.java)

            val nameEdit = mNotes[holder.adapterPosition].name
            val descriptionEdit = mNotes[holder.adapterPosition].description
            val diameterEdit = mNotes[holder.adapterPosition].diameterCircle.toString()

            intentEdit.putExtra(Fields.NAME, nameEdit)
            intentEdit.putExtra(Fields.DESCRIPTION, descriptionEdit)
            intentEdit.putExtra(Fields.DIAMETER_CIRCLE, diameterEdit)
            intentEdit.putExtra(Fields.ID, idEdit)
            intentEdit.putExtra(Fields.IMAGE_URL, imageURLEdit)

            context.startActivity(intentEdit)
        }

        noteCardView.setOnLongClickListener {
            val builder = AlertDialog.Builder(context)

            builder.setTitle("Delete note")

            builder.setMessage("Are you sure you want to delete the note?")

            builder.setPositiveButton("Yes") { _, _ ->
                removeNote(context, holder.adapterPosition, idEdit, imageURLEdit)
            }

            builder.setNegativeButton("No") { _, _ ->
                Toast.makeText(context,"Cancelled.", Toast.LENGTH_SHORT).show()
            }

            val dialog: AlertDialog = builder.create()

            dialog.show()

            true
        }
    }

    private fun removeNote(context: Context, position: Int, noteId: String, imageURL: String) {
        val ref = FirebaseDatabase.getInstance().reference
        val applesQuery: Query = ref.child("Notes").orderByChild("noteId").equalTo(noteId)

        applesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (appleSnapshot in dataSnapshot.children) {
                    appleSnapshot.ref.removeValue()
                }

                val firebaseStorage = FirebaseStorage.getInstance()
                val photoRef: StorageReference = firebaseStorage.getReferenceFromUrl(imageURL)

                photoRef.delete().addOnSuccessListener {
                    mNotes.removeAt(position)
                    notifyItemRemoved(position)

                    Toast
                        .makeText(context, "Removed!", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
    }
}