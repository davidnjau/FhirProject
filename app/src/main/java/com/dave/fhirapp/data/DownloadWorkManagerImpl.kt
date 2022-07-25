package com.dave.fhirapp.data

import com.google.android.fhir.SyncDownloadContext
import com.google.android.fhir.sync.DownloadWorkManager
import org.hl7.fhir.exceptions.FHIRException
import org.hl7.fhir.r4.model.*
import java.util.*

class DownloadWorkManagerImpl : DownloadWorkManager {

    private val resourceTypeList = ResourceType.values().map { it.name }
//    private val urls = LinkedList(listOf("Patient?address-city=NAIROBI"))
    private val urls = LinkedList(listOf("Patient?address-state=KabarakHOSPITAL14"))


    override suspend fun getNextRequestUrl(context: SyncDownloadContext): String? {

        var url = urls.poll() ?: return null
        val resourceTypeToDownload =
            ResourceType.fromCode(url.findAnyOf(resourceTypeList, ignoreCase = true)!!.second)
        context.getLatestTimestampFor(resourceTypeToDownload)?.let {
            url = affixLastUpdatedTimestamp(url!!, it)
        }
        return url
    }

    override suspend fun processResponse(response: Resource): Collection<Resource> {

        // As per FHIR documentation :
        // If the search fails (cannot be executed, not that there are no matches), the
        // return value SHALL be a status code 4xx or 5xx with an OperationOutcome.
        // See https://www.hl7.org/fhir/http.html#search for more details.
        if (response is OperationOutcome) {
            throw FHIRException(response.issueFirstRep.diagnostics)
        }

        // If the resource returned is a List containing Patients, extract Patient references and fetch
        // all resources related to the patient using the $everything operation.
        if (response is ListResource) {
            for (entry in response.entry) {
                val reference = Reference(entry.item.reference)
                if (reference.referenceElement.resourceType.equals("Patient")) {
                    val patientUrl = "${entry.item.reference}/\$everything"
                    urls.add(patientUrl)
                }
            }
        }

        // If the resource returned is a Bundle, check to see if there is a "next" relation referenced
        // in the Bundle.link component, if so, append the URL referenced to list of URLs to download.
        if (response is Bundle) {
            val nextUrl = response.link.firstOrNull { component -> component.relation == "next" }?.url
            if (nextUrl != null) {
                urls.add(nextUrl)
            }
        }

        // Finally, extract the downloaded resources from the bundle.
        var bundleCollection: Collection<Resource> = mutableListOf()
        if (response is Bundle && response.type == Bundle.BundleType.SEARCHSET) {
            bundleCollection = response.entry.map { it.resource }
        }
        return bundleCollection

    }

    private fun affixLastUpdatedTimestamp(url: String, lastUpdate: String): String{

        var downloadUrl = url

        // Affix lastUpdate to a $everything query using _since as per:
        // https://hl7.org/fhir/operation-patient-everything.html
        if (downloadUrl.contains("\$everything")){
            downloadUrl = "$downloadUrl?_since=$lastUpdate"
        }

        // Affix lastUpdate to non-$everything queries as per:
        // https://hl7.org/fhir/operation-patient-everything.html
        if (!downloadUrl.contains("\$everything")){
            downloadUrl = "$downloadUrl&_lastUpdate=gt$lastUpdate"
        }

        // Do not modify any URL set by a server that specifies the token of the page to return.
        if (downloadUrl.contains("&page_token")) {
            downloadUrl = url
        }

        return downloadUrl

    }
}