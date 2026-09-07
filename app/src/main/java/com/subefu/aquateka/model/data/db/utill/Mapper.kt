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
        this.id ?: 0,
        this.clientId,
        this.address,
        this.latitude,
        this.longitude,
        this.plannedDay,
        this.plannedMonth,
        this.plannedYear,
        this.actualDate,
        this.status,
        this.workType,
        this.price,
        this.parts,
        this.comment,
        this.period,
    )

fun ClientEntity.toModel()
    = Client(
        this.clientId ?: 0,
        this.name,
        this.phone,
        this.address,
        this.latitude,
        this.longitude,
        this.periodMonth,
        this.comment,
    )

fun Visit.toEntity()
    = VisitEntity(
    if(this.id == 0) null else this.id,
    this.clientId,
    this.address,
    this.latitude,
    this.longitude,
    this.planned_day,
    this.planned_month,
    this.planned_year,
    this.actual_date,
    this.status,
    this.work_type,
    this.price,
    this.parts,
    this.period,
    this.comment,
)

fun Client.toEntity()
    = ClientEntity(
    if(this.clietn_id == 0) null else this.clietn_id,
    this.name,
    this.phone,
    this.address,
    this.latitude,
    this.longitude,
    this.period_month,
    this.comment
)