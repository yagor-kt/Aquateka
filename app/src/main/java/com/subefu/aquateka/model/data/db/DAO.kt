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
import kotlinx.coroutines.flow.Flow


@Dao
interface DAO {
    //Visits
    @Query("Select * from visit where plannedMonth = :month and plannedYear = :year")
    fun getVisitsWithClientForMonth(month: Int, year: Int): Flow<List<VisitWithClientEntity>>

    @Query("Select * from visit where clientId = :clientId order by plannedYear, plannedMonth")
    fun getVisitsByClientId(clientId: Int): Flow<List<VisitEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createVisit(visit: VisitEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateVisit(visit: VisitEntity)

    @Delete
    suspend fun deleteVisit(visit: VisitEntity)

    //Client
    @Query("Select * from client")
    fun getClients(): Flow<List<ClientEntity>>

    @Query("select * from client where clientId = :id")
    fun getClientById(id: Int): Flow<ClientEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createClient(client: ClientEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)
}