package com.example.podroznik.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.podroznik.R
import com.example.podroznik.models.Note
import kotlinx.android.synthetic.main.activity_card_view.view.*

class Adapter(private val dataArray: ArrayList<Note>): RecyclerView.Adapter<Adapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.activity_card_view, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("TAG", "onBindViewHolder")

        val name = holder.itemView.NAME_TEXT_VIEW_CARD
        val description = holder.itemView.DESCRIPTION_TEXT_VIEW_CARD
        val diameterCircle = holder.itemView.DIAMETER_CIRCLE

        name.text = dataArray[holder.adapterPosition].name
        description.text = dataArray[holder.adapterPosition].description
        diameterCircle.text = dataArray[holder.adapterPosition].diameterCircle.toString()

        Log.d("TAG", name.toString())
    }

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
    }
}