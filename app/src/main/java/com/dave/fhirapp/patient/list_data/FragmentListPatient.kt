package com.dave.fhirapp.patient.list_data

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.FhirApplication
import com.dave.fhirapp.helper.FhirFormatterClass
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import org.hl7.fhir.r4.model.QuestionnaireResponse

class FragmentListPatient : Fragment() {

    private lateinit var rootView: View

    private lateinit var fhirFormatterClass: FhirFormatterClass
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_list_patient, container, false)

        fhirEngine = FhirApplication.fhirEngine(requireContext())
        fhirFormatterClass = FhirFormatterClass(requireActivity().application, fhirEngine)

//        val patientList = fhirFormatterClass.liveSearchedPatients

        fhirFormatterClass = ViewModelProvider(this,
            FhirFormatterClass.FhirFormatterClassViewModelFactory
                (requireActivity().application, fhirEngine)
        )[FhirFormatterClass::class.java]

        fhirFormatterClass.liveSearchedPatients.observe(
            viewLifecycleOwner, Observer {
                Log.e("------", "------")
                println(it)
            }
        )

        return rootView
    }


}