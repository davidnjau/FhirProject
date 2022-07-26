package com.dave.fhirapp.patient.details.encounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.CodingObservation
import com.dave.fhirapp.helper.FormatterClass
import com.dave.fhirapp.helper.QuantityObservation
import com.dave.fhirapp.patient.details.FragmentPatientDetails
import org.hl7.fhir.r4.model.Reference

class EncounterDetails : AppCompatActivity() {

    private val formatterClass = FormatterClass()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encounter_details)

        if (savedInstanceState == null){

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, startFragmentPatient())
            ft.commit()
        }

    }

    private fun startFragmentPatient(): FragmentEncounters {
        val frag = FragmentEncounters()
        val bundle = Bundle()
        bundle.putString(FragmentEncounters.QUESTIONNAIRE_FILE_PATH_KEY, "client-registration.json")
        frag.arguments = bundle
        return frag
    }
}