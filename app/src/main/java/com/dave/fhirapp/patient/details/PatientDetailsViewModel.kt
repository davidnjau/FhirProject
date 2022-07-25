package com.dave.fhirapp.patient.details

import android.app.Application
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import ca.uhn.fhir.context.FhirContext
import com.dave.fhirapp.R
import com.dave.fhirapp.helper.*
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.get
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.search
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String): AndroidViewModel(application) {

    var livePatientData = DbPatientRecord()

    /** Emits list of [PatientDetailData]. */
    fun getPatientDetailData() {
        viewModelScope.launch { livePatientData = getPatientDetailDataModel() }
    }

    private suspend fun getPatient(): PatientItem {

        val patient = fhirEngine.get<Patient>(patientId)
        return FormatterClass().patientData(patient, 0)
    }



//    fun createEncounter(
//        patientReference: Reference,
//        encounterReference: Reference,
//        encounterId: String,
//        questionnaireResponse: QuestionnaireResponse) {
//
//        viewModelScope.launch {
//
//            val bundle =
//                ResourceMapper.extract(
//                    questionnaireResource,
//                    questionnaireResponse
//                )
//
//            bundle.entry.forEach {
//
//
//
//
////                when(val resource = it.resource) {
////                    is Observation -> {
////                        resource.resourceType = subjectReference
////                        resource.id = UUID.randomUUID().toString()
////                        resource.meta = Meta().apply {
////                            versionId = UUID.randomUUID().toString()
////                            lastUpdated = Date()
////                        }
////                        resource.code = CodeableConcept().apply {
////                            coding = listOf(Coding().apply {
////                                system = "http://loinc.org"
////                                code = "8302-2"
////                                display = "Vital signs"
////                            })
////                            text = "Questionnaire Response"
////                        }
////                        resource.subject = subjectReference
////                        resource.encounter = Reference().apply {
////                            reference = "Encounter/$encounterId"
////                        }
////                        resource.issued = Date()
////                        resource.valueStringType = "string"
////
////                    }
////                    is Encounter -> {
////                        resource.subject = subjectReference
////                        resource.id = encounterId
////                        resource.reasonCodeFirstRep.text = reason
////                        resource.reasonCodeFirstRep.codingFirstRep.code = reason
////                        resource.status = Encounter.EncounterStatus.INPROGRESS
////                        resource.partOf = basedOnReference
////
////                        saveResourceToDatabase(resource)
////                    }
////                }
//
//            }
//
//
//        }


    private suspend fun saveResourceToDatabase(resource: Resource) {
        fhirEngine.create(resource)
    }

    private suspend fun getPatientDetailDataModel():DbPatientRecord{

        val dbPatientRecord = DbPatientRecord()

        val patient = getPatient()

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
                val observationsList = getPatientObservations(encounterId)

                val dbEncounter = DbEncounter(encounterId, lastUpdated, reasonCode, observationsList)
                dbEncounterList.add(dbEncounter)
            }


        }

        dbPatientRecord.dbEncounterList = dbEncounterList

        return dbPatientRecord

    }

    //Get all observations for patient under the selected encounter
    private suspend fun getPatientObservations(encounterId: String): List<ObservationItem> {

        val observations = mutableListOf<ObservationItem>()
        fhirEngine
            .search<Observation> { filter(Observation.SUBJECT, {value = "Encounter/$encounterId/'$'everything"}) }
            .take(Int.MAX_VALUE)
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }
        return observations
    }

    //Get all encounters under this patient
    private suspend fun getEncounterDetails():List<EncounterItem>{

        val encounter = mutableListOf<EncounterItem>()
        fhirEngine
            .search<Encounter> { filter(Encounter.PATIENT, {value = "Encounter/?patient=$patientId"}) }
            .map { createEncounterItem(it, getApplication<Application>().resources) }
            .let { encounter.addAll(it) }
        return encounter
    }

    companion object{

        private fun createObservationItem(observation: Observation, resources: Resources):
                ObservationItem{

            val observationCode = observation.code.text ?: observation.code.codingFirstRep.display
            // Show nothing if no values available for datetime and value quantity.
            val dateTimeString =
                if (observation.hasEffectiveDateTimeType()) {
                    observation.effectiveDateTimeType.asStringValue()
                } else {
                    resources.getText(R.string.message_no_datetime).toString()
                }
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

            return ObservationItem(
                observation.logicalId,
                observationCode,
                valueString
            )
        }

        private fun createEncounterItem(encounter: Encounter, resources: Resources):
                EncounterItem{

//            val encounterDateTimeString =
//                if (encounter.hasPeriod()) {
//                    encounter.period.start.time.toString()
//                } else {
//                    resources.getText(R.string.message_no_datetime).toString()
//                }
//
//            val encounterType = encounter.type.firstOrNull()?.coding?.firstOrNull()?.display ?: ""
//            val encounterLocation = encounter.location.firstOrNull()?.location?.display ?: ""
//            val encounterStatus = encounter.status.display

            val lastUpdated = encounter.meta.lastUpdated.toString()
            val reasonCode = encounter.reasonCode.firstOrNull()?.text ?: ""

                return EncounterItem(
                    encounter.logicalId,
                    lastUpdated,
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

