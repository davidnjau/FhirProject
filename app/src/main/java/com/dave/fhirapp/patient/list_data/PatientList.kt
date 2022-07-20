package com.dave.fhirapp.patient.list_data

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dave.fhirapp.R

class PatientList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)
        if (savedInstanceState == null){

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentListPatient())
            ft.commit()
        }



    }
}