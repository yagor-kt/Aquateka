package com.subefu.aquateka.model.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.subefu.aquateka.model.data.entity.ClientEntity
import com.subefu.aquateka.model.data.entity.VisitEntity
import com.subefu.aquateka.model.data.entity.VisitWithClientEntity
import com.subefu.aquateka.model.domain.model.Visit
import kotlinx.coroutines.flow.Flow


@Dao
interface DAO {
    //Visits
    @Query("Select * from visit where planned_month = :month and planned_year = :year order by status desc")
    fun getVisitsWithClientForMonth(month: Int, year: Int): Flow<List<VisitWithClientEntity>>

    @Query("Select * from visit where client_id = :clientId order by planned_year desc, planned_month desc")
    fun getVisitsByClientId(clientId: Int): Flow<List<VisitEntity>>

    @Query("Select * from visit where client_id in(:clientIds)")
    fun getVisitsByClientIds(clientIds: List<Int>): List<VisitWithClientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClients(clients: List<ClientEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVisits(visits: List<VisitEntity>)

    @Query("""
        Select * from visit v where v.status in (:status) and (v.planned_year < :year or v.planned_year = :year and v.planned_month < :month)
    """)
    fun getVisitBeforeDate(status: List<String>, month: Int, year: Int): Flow<List<VisitEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createVisit(visit: VisitEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateVisit(visit: VisitEntity)

    @Delete
    suspend fun deleteVisit(visit: VisitEntity)

    //Client
    @Query("Select * from client")
    fun getClients(): Flow<List<ClientEntity>>

    @Query("select * from client where client_id = :id")
    fun getClientById(id: Int): Flow<ClientEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createClient(client: ClientEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)
}