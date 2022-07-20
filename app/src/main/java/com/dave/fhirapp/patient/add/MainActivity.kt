package com.dave.fhirapp.patient.add

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dave.fhirapp.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null){

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, startFragmentAddPatient())
            ft.commit()
        }
    }

    private fun startFragmentAddPatient(): FragmentAddPatient {
        val frag = FragmentAddPatient()
        val bundle = Bundle()
        bundle.putString(FragmentAddPatient.QUESTIONNAIRE_FILE_PATH_KEY, "new-patient-registration-paginated.json")
        frag.arguments = bundle
        return frag
    }
}