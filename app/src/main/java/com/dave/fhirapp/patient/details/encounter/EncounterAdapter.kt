package com.dave.fhirapp.patient.details.encounter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.DbEncounter
import com.dave.fhirapp.helper.FormatterClass

class EncounterAdapter(private var entryList: List<DbEncounter>?,
                       private val context: Context
) : RecyclerView.Adapter<EncounterAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPosition: TextView = itemView.findViewById(R.id.tvPosition)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = entryList?.get(position)?.id

            FormatterClass().saveSharedPreference(context, "encounterId", id.toString())

            val intent = Intent(context, EncounterDetails::class.java)
            intent.putExtra("encounterId", id)
            context.startActivity(intent)

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

        val text = entryList?.get(position)?.text

        val pos = "${position + 1}"

        holder.tvName.text = text
        holder.tvPosition.text = "#$pos"


    }

    override fun getItemCount(): Int {
        return entryList!!.size
    }

}