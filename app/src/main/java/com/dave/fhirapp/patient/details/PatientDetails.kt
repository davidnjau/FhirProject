package com.dave.fhirapp.patient.details

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.FhirApplication
import com.dave.fhirapp.helper.FhirFormatterClass
import com.google.android.fhir.FhirEngine

class PatientDetails : AppCompatActivity() {

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var fhirEngine: FhirEngine

    private lateinit var tvPatientData: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_details)

        //get value from intent
        val patientId = intent.getStringExtra("patientId").toString()

        Log.e("******", patientId)

        fhirEngine = FhirApplication.fhirEngine(this)

        tvPatientData = findViewById(R.id.tvPatientData)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(this.application,fhirEngine, patientId)
            )[PatientDetailsViewModel::class.java]

        val dbPatientRecord = patientDetailsViewModel.livePatientData
        val patientData = dbPatientRecord.dbPatientData

        val patientDataList = patientData?.patientDetailList
        if (patientDataList != null) {

            var patientInfo = ""

            for (patient in patientDataList){

                val header = patient.header
                val value = patient.value

                patientInfo += "$header: $value\n"

            }

            tvPatientData.text = patientInfo
        }




    }
}