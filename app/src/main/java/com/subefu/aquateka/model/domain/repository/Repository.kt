package com.subefu.aquateka.model.domain.repository

import com.subefu.aquateka.model.domain.model.VisitWithClient
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun getVisitWithClientForMonth(month: Int, year: Int): Flow<List<VisitWithClient>>

}