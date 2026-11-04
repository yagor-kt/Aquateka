package com.subefu.aquateka.model.domain.repository

import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.model.VisitWithClient
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun getClients(): Flow<List<Client>>
    fun getClientById(id: Int): Flow<Client?>
    suspend fun getAllIDClients(): List<Int>

    fun getVisitWithClientForMonth(month: Int, year: Int): Flow<List<VisitWithClient>>
    fun getVisitByClient(clientId: Int): Flow<List<Visit>>
    fun getVisitBeforeDate(status: List<String>, month: Int, year: Int): Flow<List<Visit>>
    suspend fun getVisitsByClientIds(clientIds: List<Int>): List<VisitWithClient>

    suspend fun insertVisits(visits: List<Visit>)
    suspend fun insertVisit(visit: Visit): Int
    suspend fun updateVisit(visit: Visit)
    suspend fun deleteVisit(visit: Visit)

    suspend fun insertClients(clients: List<Client>)
    suspend fun insertClient(client: Client): Int
    suspend fun updateClient(client: Client)
    suspend fun deleteClient(client: Client)
}