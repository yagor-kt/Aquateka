package com.subefu.aquateka.model.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.subefu.aquateka.model.data.entity.VisitEntity
import com.subefu.aquateka.model.data.entity.VisitWithClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitsDao {

    @Query("Select * from visit where client_id = :clientId order by planned_year desc, planned_month desc")
    fun getVisitsByClientIdSorted(clientId: Int): Flow<List<VisitEntity>>

    @Query("Select * from visit where client_id in(:clientIds)")
    suspend fun getVisitsByClientIds(clientIds: List<Int>): List<VisitWithClientEntity>

    @Query("Select * from visit where planned_month = :month and planned_year = :year order by status desc")
    fun getVisitsWithClientForMonth(month: Int, year: Int): Flow<List<VisitWithClientEntity>>

    @Query("""
        Select * from visit v where v.status in (:status) and (v.planned_year < :year or v.planned_year = :year and v.planned_month < :month)
    """)
    fun getVisitBeforeDate(status: List<String>, month: Int, year: Int): Flow<List<VisitEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVisit(visit: VisitEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVisits(visits: List<VisitEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateVisit(visit: VisitEntity)

    @Delete
    suspend fun deleteVisit(visit: VisitEntity)
}