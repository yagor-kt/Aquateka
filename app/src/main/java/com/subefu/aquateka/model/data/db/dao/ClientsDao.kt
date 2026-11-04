package com.subefu.aquateka.model.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.subefu.aquateka.model.data.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientsDao {

    @Query("Select * from client")
    fun getClients(): Flow<List<ClientEntity>>

    @Query("select * from client where client_id = :id")
    fun getClientById(id: Int): Flow<ClientEntity?>

    @Query("Select client_id from client")
    suspend fun getAllIDClients(): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClient(client: ClientEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)
}