package com.subefu.aquateka.model.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.subefu.aquateka.model.domain.MyConst

@Entity(
    tableName = "Visit",
    indices = [Index(value = ["id", "client_id"])],
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["client_id"],
            childColumns = ["client_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ]
)
data class VisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    @ColumnInfo(index = true, name = "client_id")
    val clientId: Int,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "planned_day", defaultValue = "1")
    val plannedDay: Int = 1,
    @ColumnInfo(name = "planned_month")
    val plannedMonth: Int,
    @ColumnInfo(name = "planned_year")
    val plannedYear: Int,
    @ColumnInfo(name = "actual_date")
    val actualDate: Int,
    val status: String,
    @ColumnInfo(name = "work_type")
    val workType: String,
    val price: Int,
    val parts: String,
    val period: Int,
    val comment: String,
    @ColumnInfo(defaultValue = "BLACK")
    val color: String = "BLACK"
)
