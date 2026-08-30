package com.subefu.aquateka.model.domain.repository

import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.model.VisitWithClient
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun getVisitWithClientForMonth(month: Int, year: Int): Flow<List<VisitWithClient>>
    fun getClients(): Flow<List<Client>>

    fun getVisitByClient(clientId: Int): Flow<List<Visit>>
    fun getClientById(id: Int): Flow<Client?>

    fun getVisitBeforeDate(status: List<String>, month: Int, year: Int): Flow<List<Visit>>

    suspend fun insertVisit(visit: Visit): Int
    suspend fun insertClient(client: Client): Int

    suspend fun updateVisit(visit: Visit)
    suspend fun updateClient(client: Client)

    suspend fun deleteVisit(visit: Visit)
    suspend fun deleteClient(client: Client)
}