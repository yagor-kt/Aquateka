package com.subefu.aquateka.model.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Visit",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["clientId"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ]
)
data class VisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    @ColumnInfo(index = true)
    val clientId: Int,
    val address: String?,
    val latitude: Float,
    val longitude: Float,
    val plannedMonth: Int,
    val plannedYear: Int,
    val actualDate: Int,
    val status: String,
    val workType: String,
    val price: Int,
    val parts: String,
    val period: Int,
    val comment: String
)
