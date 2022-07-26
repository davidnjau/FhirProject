package com.dave.fhirapp.patient.add

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.dave.fhirapp.helper.FhirApplication
import com.dave.fhirapp.helper.FormatterClass
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.util.*
import kotlin.collections.ArrayList

class AddPatientViewModel(application: Application, private val state: SavedStateHandle) :AndroidViewModel(application){

    val questionnaire : String
        get() = getQuestionnaireJson()
    val isPatientSaved = MutableLiveData<Boolean>()

    private val questionnaireResource : Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire) as
                    Questionnaire
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)
    private var questionnaireJson : String? = null

    fun savePatient(questionnaireResponse: QuestionnaireResponse){

        viewModelScope.launch {
            if (QuestionnaireResponseValidator.validateQuestionnaireResponse(
                    questionnaireResource, questionnaireResponse, getApplication())
                    .values.flatten().any{
                        !it.isValid}) {


                isPatientSaved.value = false
                return@launch
            }


            val entry = ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
            if (entry.resource !is Patient){
                return@launch
            }

            val patient = entry.resource as Patient

            val addressList = ArrayList<Address>()
            val address = Address()
            address.state = "KabarakHOSPITAL15"
            address.city = "Kabarak"
            addressList.add(address)

            patient.address = addressList

            patient.id = FormatterClass().generateUuid()
            fhirEngine.create(patient)


            isPatientSaved.value = true
        }

    }

    private fun getQuestionnaireJson():String{
        questionnaireJson?.let { return it!! }

        questionnaireJson = readFileFromAssets(state[FragmentAddPatient.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaire!!
    }

    private fun readFileFromAssets(fileName : String): String{
        return getApplication<Application>().assets.open(fileName).bufferedReader().use {
            it.readText()
        }

    }






}