package com.dave.fhirapp.helper

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.dave.fhirapp.R
import org.hl7.fhir.r4.model.Patient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class FormatterClass {

    @RequiresApi(Build.VERSION_CODES.O)
    fun patientData(patient: Patient, position: Int):PatientItem{

//        Log.e("*******", "------")
//        println(patient)

        return patient.toPatientItem(position)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Patient.toPatientItem(position: Int): PatientItem {
        // Show nothing if no values available for gender and date of birth.
        val patientId = if (hasIdElement()) idElement.idPart else ""

//        Log.e("------", patientId)

        val name = if (hasName()) name[0].nameAsSingleString else ""
        val gender = if (hasGenderElement()) genderElement.valueAsString else ""
        val dob =
            if (hasBirthDateElement())
                LocalDate.parse(birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
            else null
        val phone = if (hasTelecom()) telecom[0].value else ""
        val city = if (hasAddress()) address[0].city else ""
        val country = if (hasAddress()) address[0].country else ""
        val isActive = active
        val html: String = if (hasText()) text.div.valueAsString else ""

        return PatientItem(
            id = position.toString(),
            resourceId = patientId,
            name = name,
            gender = gender ?: "",
            dob = dob,
            phone = phone ?: "",
            city = city ?: "",
            country = country ?: "",
            isActive = isActive,
            html = html
        )
    }

    fun generateUuid():String{
        return UUID.randomUUID().toString()
    }

    fun saveSharedPreference(
        context: Context,
        sharedKey: String,
        sharedValue: String
    ) {

        val appName = context.getString(R.string.app_name)
        val sharedPreferences = context.getSharedPreferences(appName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(sharedKey, sharedValue)
        editor.apply()
    }

    fun retrieveSharedPreference(
        context: Context,
        sharedKey: String
    ): String? {

        val appName = context.getString(R.string.app_name)

        val sharedPreferences = context.getSharedPreferences(appName, Context.MODE_PRIVATE)
        return sharedPreferences.getString(sharedKey, null)

    }

}