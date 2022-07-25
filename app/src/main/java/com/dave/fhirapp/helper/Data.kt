package com.dave.fhirapp.helper

import org.hl7.fhir.r4.model.Observation
import java.time.LocalDate

/** The Patient's details for display purposes. */
data class PatientItem(
    val id: String,
    val resourceId: String,
    val name: String,
    val gender: String,
    val dob: LocalDate? = null,
    val phone: String,
    val city: String,
    val country: String,
    val isActive: Boolean,
    val html: String,
    var risk: String? = "",
) {
    override fun toString(): String = name
}

data class DbPatientData(
    val id: String,
    val patientDetailList : List<PatientProperty>
)

data class PatientProperty(val header: String, val value: String)

data class EncounterItem(
    val id: String,
    val lastUpdated: String,
    val reasonCode: String
)

/** The Observation's details for display purposes. */
data class ObservationItem(
    val id: String,
    val code: String,
    val value: String
) {
    override fun toString(): String = code
}

data class DbPatientRecord(
    var dbPatientData: DbPatientData? = null,
    var dbEncounterList: List<DbEncounter>? = null,
)
data class DbEncounter(
    val id: String,
    val lastUpdated: String,
    val reasonCode: String,
    val observationList : List<ObservationItem>
)
data class CodingObservation(
    val code: String,
    val display: String,
    val value: String,
)
data class QuantityObservation(
    val code: String,
    val display: String,
    val value: String,
    val unit: String,
)