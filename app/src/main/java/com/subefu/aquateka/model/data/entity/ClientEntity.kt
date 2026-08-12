package com.subefu.aquateka.model.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Client")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val clientId: Int?,
    val name: String,
    val phone: String,
    val address: String?,
    val latitude: Float,
    val longitude: Float,
    val periodMonth: Int?,
    val comment: String
)