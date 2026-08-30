package com.subefu.aquateka.model.data.repository

import android.content.Context
import android.util.Log
import com.subefu.aquateka.model.data.db.DAO
import com.subefu.aquateka.model.data.db.utill.toEntity
import com.subefu.aquateka.model.data.db.utill.toModel
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.model.domain.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RepositoryImpl(val dao: DAO): Repository {

    override fun getVisitWithClientForMonth(month: Int, year: Int): Flow<List<VisitWithClient>> {
        return dao.getVisitsWithClientForMonth(month, year)
            .map { it.map { it.toModel() } }
    }

    override fun getVisitByClient(clientId: Int): Flow<List<Visit>> {
        return dao.getVisitsByClientId(clientId)
            .map { it.map { it.toModel() } }
    }

    override fun getClients(): Flow<List<Client>> {
        return dao.getClients()
            .map { it.map { it.toModel() } }
    }

    override fun getClientById(id: Int): Flow<Client?> {
        return dao.getClientById(id)
            .map { it?.toModel() }
    }

    override fun getVisitBeforeDate(status: List<String>, month: Int, year: Int): Flow<List<Visit>> {
        return dao.getVisitBeforeDate(status, month, year)
            .map { it.map { it.toModel() } }
    }

    override suspend fun deleteVisit(visit: Visit) {
        dao.deleteVisit(visit.toEntity())
    }

    override suspend fun updateVisit(visit: Visit) {
        dao.updateVisit(visit.toEntity())
    }

    override suspend fun insertVisit(visit: Visit): Int {
        return dao.createVisit(visit.toEntity()).toInt()
    }


    override suspend fun deleteClient(client: Client) {
        dao.deleteClient(client.toEntity())
    }

    override suspend fun updateClient(client: Client) {
        dao.updateClient(client.toEntity())
        Log.d("MYRepImpl", "update client: $client")
    }

    override suspend fun insertClient(client: Client): Int {
        return dao.createClient(client.toEntity()).toInt()
    }
}