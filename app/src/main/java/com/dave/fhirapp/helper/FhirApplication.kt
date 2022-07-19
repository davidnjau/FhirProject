package com.dave.fhirapp.helper

import android.app.Application
import android.content.Context
import android.util.Log
import com.dave.fhirapp.data.FhirPeriodicSyncWorker
import com.google.android.fhir.*
import com.google.android.fhir.sync.Sync

class FhirApplication : Application (){

    private val fhirEngine: FhirEngine by lazy { constructFhirEngine() }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG){
            Log.e("----", "")
        }
        FhirEngineProvider.init(
            FhirEngineConfiguration(
                enableEncryptionIfSupported = true,
                DatabaseErrorStrategy.RECREATE_AT_OPEN,
                ServerConfiguration("https://hapi.fhir.org/baseR4/")
            )
        )
        Sync.oneTimeSync<FhirPeriodicSyncWorker>(this)

    }

    private fun constructFhirEngine(): FhirEngine{
        return FhirEngineProvider.getInstance(this)
    }

    companion object{
        fun fhirEngine(context: Context) = (context.applicationContext as FhirApplication).fhirEngine
    }
}