package com.dave.fhirapp.patient.list_data

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.PatientItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class PatientsAdapter(private var entryList: List<PatientItem>?,
                      private val context: Context
) : RecyclerView.Adapter<PatientsAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPosition: TextView = itemView.findViewById(R.id.tvPosition)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition



        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.patient_list_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val name = entryList?.get(position)?.name

        val pos = "${position + 1}"

        holder.tvName.text = name
        holder.tvPosition.text = "#$pos"


    }

    override fun getItemCount(): Int {
        return entryList!!.size
    }

}