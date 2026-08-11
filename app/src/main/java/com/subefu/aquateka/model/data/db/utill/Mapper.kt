package com.subefu.aquateka.model.data.db.utill

import com.subefu.aquateka.model.data.entity.ClientEntity
import com.subefu.aquateka.model.data.entity.VisitEntity
import com.subefu.aquateka.model.data.entity.VisitWithClientEntity
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.model.VisitWithClient

fun VisitWithClientEntity.toModel() = VisitWithClient(
    this.visit.toModel(),
    this.client.toModel()
)

fun VisitEntity.toModel()
    = Visit(
     this.id,
        this.clientId,
        this.plannedMonth,
        this.plannedYear,
        this.actualDate,
        this.status,
        this.workType,
        this.price,
        this.parts,
        this.comment,
        this.createdAt,
        this.updatedAt,
    )

fun ClientEntity.toModel()
    = Client(
        this.clientId,
        this.name,
        this.phone,
        this.address,
        this.latitude,
        this.longitude,
        this.periodMonth,
        this.comment,
        this.createAt,
    )