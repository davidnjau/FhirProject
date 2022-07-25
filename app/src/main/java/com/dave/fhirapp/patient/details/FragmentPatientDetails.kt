package com.dave.fhirapp.patient.details

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.CodingObservation
import com.dave.fhirapp.helper.FormatterClass
import com.dave.fhirapp.helper.QuantityObservation
import com.dave.fhirapp.patient.add.AddPatientViewModel
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference

class FragmentPatientDetails : Fragment() {

    private lateinit var rootView: View
    private val viewModel: AddPatientDetailsViewModel by viewModels()
    private val formatterClass = FormatterClass()

//    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_details_patient, container, false)

        patientId = formatterClass.retrieveSharedPreference(requireContext(), "patientId").toString()


//        patientDetailsViewModel = ViewModelProvider(this,
//            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
//        )[PatientDetailsViewModel::class.java]

        updateArguments()

        if (savedInstanceState == null){
            addQuestionnaireFragment()
        }

        rootView.findViewById<Button>(R.id.btnAddEncounter).setOnClickListener {
            showDialog()
        }

        return rootView
    }

    private fun showDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Encounter")

        val input = EditText(requireContext())
        input.hint = "Enter Encounter Name"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            // Here you get get input text from the Edittext
            var txtEncounter = input.text.toString()
            if (txtEncounter.isNotEmpty()){

                CoroutineScope(Dispatchers.IO).launch {

                    val encounterId = formatterClass.generateUuid()
                    val patientReference = Reference("Patient/$patientId")

                    val questionnaireFragment =
                        childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
                    val questionnaireResponse = questionnaireFragment.getQuestionnaireResponse()

                    val dataCodeList = ArrayList<CodingObservation>()
                    val codingObservation = CodingObservation(
                        "3141-9", "Current Weight","Cup")
                    dataCodeList.add(codingObservation)

                    val dataQuantityList = ArrayList<QuantityObservation>()
                    val quantityObservation = QuantityObservation(
                        "3141-9", "Current Weight","10", "g")
                    dataQuantityList.add(quantityObservation)

                    viewModel.createEncounter(
                        patientReference,
                        encounterId,
                        questionnaireResponse,
                        dataCodeList, dataQuantityList,
                        txtEncounter
                    )




                }


            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }


    private fun updateArguments(){
        requireArguments()
            .putString(QUESTIONNAIRE_FILE_PATH_KEY, "client-registration.json")
    }

    private fun addQuestionnaireFragment(){
        val fragment = QuestionnaireFragment()
        fragment.arguments =
            bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
        childFragmentManager.commit {
            add(R.id.add_patient_container, fragment, QUESTIONNAIRE_FRAGMENT_TAG)
        }
    }


    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

}