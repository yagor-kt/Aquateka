package com.subefu.aquateka.model.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Client",
    indices = [Index(value = ["client_id"])])
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "client_id")
    val clientId: Int?,
    val name: String,
    val phone: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "period_month")
    val periodMonth: Int?,
    val comment: String
)