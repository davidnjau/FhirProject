package com.dave.fhirapp.patient.details.encounter

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.CodingObservation
import com.dave.fhirapp.helper.FhirApplication
import com.dave.fhirapp.helper.FormatterClass
import com.dave.fhirapp.helper.QuantityObservation
import com.dave.fhirapp.patient.details.AddPatientDetailsViewModel
import com.dave.fhirapp.patient.details.FragmentPatientDetails
import com.dave.fhirapp.patient.details.PatientDetailsViewModel
import com.dave.fhirapp.patient.details.encounter.EncounterAdapter
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.QuestionnaireFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Reference

class FragmentEncounters : Fragment() {

    private lateinit var rootView: View
    private val viewModel: AddPatientDetailsViewModel by viewModels()
    private val formatterClass = FormatterClass()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    private lateinit var tvObservation: TextView

    private lateinit var encounterId : String

    private lateinit var etObservationCode : EditText
    private lateinit var etObservationDisplay : EditText
    private lateinit var etObservationValue : EditText
    private lateinit var etObservationUnit : EditText

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_encounters, container, false)

        patientId = formatterClass.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        tvObservation = rootView.findViewById(R.id.tvObservation)


        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        updateArguments()

        if (savedInstanceState == null){
            addQuestionnaireFragment()
        }


        encounterId = formatterClass.retrieveSharedPreference(requireContext(), "encounterId").toString()

        etObservationCode = rootView.findViewById(R.id.etObservationCode)
        etObservationDisplay = rootView.findViewById(R.id.etObservationDisplay)
        etObservationValue = rootView.findViewById(R.id.etObservationValue)
        etObservationUnit = rootView.findViewById(R.id.etObservationUnit)

        rootView.findViewById<Button>(R.id.btnObservations).setOnClickListener {


            val questionnaireFragment =
                childFragmentManager.findFragmentByTag(FragmentPatientDetails.QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
            val questionnaireResponse = questionnaireFragment.getQuestionnaireResponse()

            val observationCode = etObservationCode.text.toString()
            val observationDisplay = etObservationDisplay.text.toString()
            val observationValue = etObservationValue.text.toString()
            val observationUnit = etObservationUnit.text.toString()

            if (!TextUtils.isEmpty(observationCode) &&
                !TextUtils.isEmpty(observationDisplay) &&
                !TextUtils.isEmpty(observationValue) &&
                !TextUtils.isEmpty(observationUnit)){

                val dataQuantityList = ArrayList<QuantityObservation>()
                val dataCodeList = ArrayList<CodingObservation>()

                if (!TextUtils.isEmpty(observationUnit)){

                    val quantityObservation = QuantityObservation(
                        observationCode,
                        observationDisplay,
                        observationValue,
                        "g")
                    dataQuantityList.add(quantityObservation)

                }else{
                    val codingObservation = CodingObservation(
                        observationCode,
                        observationDisplay,
                        observationValue)
                    dataCodeList.add(codingObservation)

                }

                val patientReference = Reference("Patient/$patientId")


                viewModel.updateEncounter(
                    patientReference,
                    encounterId,
                    questionnaireResponse,
                    dataCodeList,
                    dataQuantityList
                )



            }else{
                Toast.makeText(requireContext(), "Please enter data", Toast.LENGTH_SHORT).show()
            }


        }


        return rootView
    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch{

            val observationList = patientDetailsViewModel.getObservationsEncounter(encounterId)
            var observationString = ""

            observationList.forEach {

                val code = it.code
                val text = it.text
                val value = it.value
                val id = it.id

                observationString = "$observationString$text : $value \n"

            }

            CoroutineScope(Dispatchers.Main).launch {
                tvObservation.text = observationString
            }

        }


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