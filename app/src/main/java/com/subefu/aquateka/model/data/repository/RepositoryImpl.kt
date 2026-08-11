package com.subefu.aquateka.model.data.repository

import android.content.Context
import com.subefu.aquateka.model.data.db.DAO
import com.subefu.aquateka.model.data.db.utill.toModel
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.model.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RepositoryImpl(val dao: DAO): Repository {
    override fun getVisitWithClientForMonth(month: Int, year: Int): Flow<List<VisitWithClient>> {
        return dao.getVisitsWithClientForMonth(month, year).map { it.map { it.toModel() } }
    }
}