package com.subefu.aquateka.model.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class VisitWithClientEntity(
    @Embedded val visit: VisitEntity,
    @Relation(
        parentColumn = "clientId",
        entityColumn = "clientId"
    )
    val client: ClientEntity
)
