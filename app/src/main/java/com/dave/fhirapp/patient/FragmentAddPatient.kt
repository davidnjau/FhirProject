package com.dave.fhirapp.patient

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
import com.dave.fhirapp.R
import com.google.android.fhir.datacapture.QuestionnaireFragment
import org.hl7.fhir.r4.model.QuestionnaireResponse

class FragmentAddPatient : Fragment() {

    private lateinit var rootView: View
    private val viewModel: AddPatientViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_add_patient, container, false)

        updateArguments()

        if (savedInstanceState == null){
            addQuestionnaireFragment()
        }

        rootView.findViewById<Button>(R.id.btnSaveButton).setOnClickListener {

            val questionnaireFragment = childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
            savePatient(questionnaireFragment.getQuestionnaireResponse())

            Log.e("*******4 ", "questionnaireResponse")
            println(questionnaireFragment.getQuestionnaireResponse())
        }


        observePatientSaveAction()

        return rootView
    }

    private fun savePatient(questionnaireResponse: QuestionnaireResponse) {

        Log.e("*******3 ", "questionnaireResponse")
        println(questionnaireResponse)

        viewModel.savePatient(questionnaireResponse)

    }

    private fun updateArguments(){
        requireArguments()
            .putString(QUESTIONNAIRE_FILE_PATH_KEY, "new-patient-registration-paginated.json")
    }

    private fun addQuestionnaireFragment(){
        val fragment = QuestionnaireFragment()
        fragment.arguments =
            bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
        childFragmentManager.commit {
            add(R.id.add_patient_container, fragment, QUESTIONNAIRE_FRAGMENT_TAG)
        }
    }

    private fun observePatientSaveAction(){
        viewModel.isPatientSaved.observe(viewLifecycleOwner){
            if (!it){
                Toast.makeText(requireContext(), "Inputs are missing.", Toast.LENGTH_SHORT).show()
                return@observe
            }
            Toast.makeText(requireContext(), "Patient is saved.", Toast.LENGTH_SHORT).show()
            //Do Something
        }
    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }

}