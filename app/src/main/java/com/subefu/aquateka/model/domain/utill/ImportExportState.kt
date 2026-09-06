package com.subefu.aquateka.model.domain.utill

import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit

sealed class ImportExportState {
        object Idle : ImportExportState()
        data class SuccessExportVisits(val cvs: String) : ImportExportState()
        data class SuccessImportVisits(val visits: List<Visit>) : ImportExportState()
        data class SuccessExportClients(val cvs: String) : ImportExportState()
        data class SuccessImportClients(val visits: List<Client>) : ImportExportState()
        data class Error(val message: String) : ImportExportState()
}