package com.dave.fhirapp.patient.details

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
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
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.CodingObservation
import com.dave.fhirapp.helper.FhirApplication
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

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel

    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    private lateinit var txtPatientName: TextView
    private lateinit var tvEncounters: TextView
    private lateinit var etEncounterName: EditText

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_details_patient, container, false)

        patientId = formatterClass.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        txtPatientName = rootView.findViewById(R.id.txtPatientName)
        tvEncounters = rootView.findViewById(R.id.tvEncounters)
        etEncounterName = rootView.findViewById(R.id.etEncounterName)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        updateArguments()

        if (savedInstanceState == null){
            addQuestionnaireFragment()
        }

        rootView.findViewById<Button>(R.id.btnAddEncounter).setOnClickListener {
            showDialog()
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {
            getPatientDetails()
        }
    }

    private fun getPatientDetails() {

        CoroutineScope(Dispatchers.IO).launch {

            val patientRecord = patientDetailsViewModel.getPatientData()

            val patientDetails = patientRecord.dbPatientData
            if (patientDetails != null){

                val list = patientDetails.patientDetailList
                var headerData = ""

                list.forEach {

                    val header = it.header
                    val value = it.value

                    headerData = "$headerData$header: $value\n"
                }

                val encounterList = patientRecord.dbEncounterList
                var encounterData = ""

                encounterList.forEach {

                    val reasonCode = it.reasonCode
                    val encounterText = it.text
                    val observationList = it.observationList
                    var observationData = ""

//                    Log.e("----- ", encounterText)

                    observationList.forEach { observation ->

                        val text = observation.text
                        val value = observation.value

                        observationData = "$observationData$text: $value\n"

                    }

                    encounterData = "## $encounterData$encounterText\n\n Observations: $observationData\n"

                }


                CoroutineScope(Dispatchers.Main).launch {
                    txtPatientName.text = headerData
                    tvEncounters.text = encounterData
                }

            }




        }

    }

    private fun showDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Save Encounter")
        builder.setMessage("Save Encounter Name")

        builder.setPositiveButton("OK") { dialog, which ->
            // Here you get get input text from the Edittext
            var txtEncounterName = etEncounterName.text.toString()

            if (!TextUtils.isEmpty(txtEncounterName)){

                CoroutineScope(Dispatchers.IO).launch {

                    val encounterId = formatterClass.generateUuid()
                    val patientReference = Reference("Patient/$patientId")

                    val questionnaireFragment =
                        childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
                    val questionnaireResponse = questionnaireFragment.getQuestionnaireResponse()

                    val dataQuantityList = ArrayList<QuantityObservation>()
//                    val quantityObservation = QuantityObservation(
//                        txtCode,
//                        txtDisplay,
//                        txtValue,
//                        "g")
//                    dataQuantityList.add(quantityObservation)

                    val dataCodeList = ArrayList<CodingObservation>()

                    viewModel.createEncounter(
                        patientReference,
                        encounterId,
                        questionnaireResponse,
                        dataCodeList,
                        dataQuantityList,
                        txtEncounterName
                    )




                }


            }else{
                Toast.makeText(requireContext(), "Please enter encounter name", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun validateValue(valueList: List<EditText>): Boolean {

        valueList.forEach {

            val value = it.text.toString()
            if (!TextUtils.isEmpty(value)){
                it.error = "Required"
                return false
            }

        }
        return true

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