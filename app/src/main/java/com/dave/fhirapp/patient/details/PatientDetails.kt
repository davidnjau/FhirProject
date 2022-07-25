package com.dave.fhirapp.patient.details

import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.FhirApplication
import com.dave.fhirapp.helper.FhirFormatterClass
import com.dave.fhirapp.helper.FormatterClass
import com.dave.fhirapp.patient.add.FragmentAddPatient
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import org.hl7.fhir.r4.model.Reference

class PatientDetails : AppCompatActivity() {

//    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
//    private lateinit var fhirEngine: FhirEngine

    private lateinit var tvPatientData: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_details)


        tvPatientData = findViewById(R.id.tvPatientData)

        if (savedInstanceState == null){

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, startFragmentPatient())
            ft.commit()
        }
//
//        patientDetailsViewModel = ViewModelProvider(this,
//            PatientDetailsViewModel.PatientDetailsViewModelFactory(this.application,fhirEngine, patientId)
//            )[PatientDetailsViewModel::class.java]

//        val dbPatientRecord = patientDetailsViewModel.livePatientData
//        val patientData = dbPatientRecord.dbPatientData

//        val patientDataList = patientData?.patientDetailList
//        if (patientDataList != null) {
//
//            var patientInfo = ""
//
//            for (patient in patientDataList){
//
//                val header = patient.header
//                val value = patient.value
//
//                patientInfo += "$header: $value\n"
//
//            }
//
//            tvPatientData.text = patientInfo
//        }



    }

    private fun startFragmentPatient(): FragmentPatientDetails {
        val frag = FragmentPatientDetails()
        val bundle = Bundle()
        bundle.putString(FragmentPatientDetails.QUESTIONNAIRE_FILE_PATH_KEY, "client-registration.json")
        frag.arguments = bundle
        return frag
    }



}