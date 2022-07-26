package com.dave.fhirapp.patient.details

import android.app.Application
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.*
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.get
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String): AndroidViewModel(application) {

    var livePatientData = MutableLiveData<DbPatientRecord>()

    init {
        updatePatientData()
    }

    private fun updatePatientData() {

        viewModelScope.launch {
            getPatientDetailData()
        }
    }

    /** Emits list of [PatientDetailData]. */
    private fun getPatientDetailData() {
//        viewModelScope.launch { livePatientData = getPatientDetailDataModel() }
    }

    fun getPatientData() = runBlocking{
        getPatientDetailDataModel()
    }

    fun getObservationsEncounter(encounterId: String) = runBlocking{
        getPatientObservations(encounterId)
    }

    private suspend fun getPatient(): PatientItem {

        val patient = getPatientResource()
        return FormatterClass().patientData(patient, 0)
    }

    private suspend fun getPatientResource(): Patient {

        return fhirEngine.get(patientId)
    }

    private suspend fun getPatientDetailDataModel():DbPatientRecord{

        val dbPatientRecord = DbPatientRecord()

        val patientResource = getPatientResource()
        val patient = FormatterClass().patientData(patientResource, 0)

        val encountersList = getEncounterDetails()

        val patientDetailList = mutableListOf<PatientProperty>()

        patient.let {

            val name = PatientProperty("Full name", it.name)
            val dob = PatientProperty("Date of birth", "it.dob.toString()")
            val gender = PatientProperty("Gender", "it.gender")
            val phone = PatientProperty("Phone", "it.phone")
            val city = PatientProperty("City", "it.city")
            val country = PatientProperty("Country", "it.country")

            patientDetailList.addAll(listOf(name, dob, gender, phone, city, country))
            val dbPatientData = DbPatientData(patientId, patientDetailList)
            dbPatientRecord.dbPatientData = dbPatientData


        }

        val dbEncounterList = mutableListOf<DbEncounter>()

        if (encountersList.isNotEmpty()) {

            for (encounter in encountersList){

                val encounterId = encounter.id
                val lastUpdated = encounter.lastUpdated
                val reasonCode = encounter.reasonCode
                val text = encounter.text
                val observationsList = getPatientObservations(encounterId)

                val dbEncounter = DbEncounter(encounterId,text, lastUpdated, reasonCode, observationsList)
                dbEncounterList.add(dbEncounter)
            }


        }

        dbPatientRecord.dbEncounterList = dbEncounterList

//        val dbPatientRecordLive = MutableLiveData<DbPatientRecord>()
//        dbPatientRecordLive.value = dbPatientRecord

        return dbPatientRecord

    }

    //Get all observations for patient under the selected encounter
    private suspend fun getPatientObservations(encounterId: String): List<ObservationItem> {

        val observations = mutableListOf<ObservationItem>()
        fhirEngine
            .search<Observation> {
                filter(
                    Observation.ENCOUNTER,
                    {value = "Encounter/$encounterId"}
                )
            }
            .take(Int.MAX_VALUE)
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }
        return observations
    }


    //Get all encounters under this patient
    private suspend fun getEncounterDetails():List<EncounterItem>{

        val encounter = mutableListOf<EncounterItem>()

        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createEncounterItem(it, getApplication<Application>().resources) }
            .let { encounter.addAll(it) }

        Log.e("******* ", "*******")
        println("--patientId--$patientId")
        println(encounter)

        return encounter
    }

    companion object{

        private fun createObservationItem(observation: Observation, resources: Resources): ObservationItem{

            // Show nothing if no values available for datetime and value quantity.
            val dateTimeString =
                if (observation.hasEffectiveDateTimeType()) {
                    observation.effectiveDateTimeType.asStringValue()
                } else {
                    resources.getText(R.string.message_no_datetime).toString()
                }


            val id = observation.logicalId
            val text = observation.code.text ?: observation.code.codingFirstRep.display
            val code = observation.code.coding[0].code
            val value =
                if (observation.hasValueQuantity()) {
                    observation.valueQuantity.value.toString()
                } else if (observation.hasValueCodeableConcept()) {
                    observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
                } else {
                    ""
                }
            val valueUnit =
                if (observation.hasValueQuantity()) {
                    observation.valueQuantity.unit ?: observation.valueQuantity.code
                } else {
                    ""
                }
            val valueString = "$value $valueUnit"

//            Log.e("*_*_*_*_*","--------")
//            Log.e("1",id)
//            Log.e("2",code)
//            Log.e("3",text)
//            Log.e("4",valueString)

            return ObservationItem(
                id,
                code,
                text,
                valueString
            )
        }

        private fun createEncounterItem(encounter: Encounter, resources: Resources): EncounterItem{

            val encounterDateTimeString =
                if (encounter.hasPeriod()) {
                    encounter.period.start.time.toString()
                } else {
                    resources.getText(R.string.message_no_datetime).toString()
                }

            val encounterType = encounter.type.firstOrNull()?.coding?.firstOrNull()?.display ?: ""
            val encounterLocation = encounter.location.firstOrNull()?.location?.display ?: ""
            val encounterStatus = encounter.status.display

            var lastUpdatedValue = ""
            val lastUpdated = encounter.meta.lastUpdated
            lastUpdatedValue = lastUpdated?.toString() ?: ""

            val reasonCode = encounter.reasonCode.firstOrNull()?.text ?: ""

            var textValue = ""

            if(encounter.reasonCode.size > 0){

                val text = encounter.reasonCode[0].text
                val textString = encounter.reasonCode[0].text?.toString() ?: ""
                val textStringValue = encounter.reasonCode[0].coding[0].code ?: ""

                textValue = if (textString != "") {
                    textString
                }else if (textStringValue != ""){
                    textStringValue
                }else text ?: ""

            }



            return EncounterItem(
                encounter.logicalId,
                textValue,
                lastUpdatedValue,
                reasonCode
            )
        }
    }

    class PatientDetailsViewModelFactory(
        private val application: Application,
        private val fhirEngine: FhirEngine,
        private val patientId: String
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PatientDetailsViewModel(application, fhirEngine, patientId) as T
        }

    }


}

