package com.dave.fhirapp.data

import android.content.Context
import androidx.work.WorkerParameters
import com.dave.fhirapp.helper.FhirApplication
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.sync.DownloadWorkManager
import com.google.android.fhir.sync.FhirSyncWorker

class FhirPeriodicSyncWorker(appContext: Context, workerParameters: WorkerParameters):
    FhirSyncWorker(appContext, workerParameters) {

    override fun getDownloadWorkManager(): DownloadWorkManager {
        return DownloadWorkManagerImpl()
    }

    override fun getFhirEngine(): FhirEngine = FhirApplication.fhirEngine(applicationContext)


}