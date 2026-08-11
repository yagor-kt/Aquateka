package com.subefu.aquateka.model.domain.model


data class Visit(
    val id: Int,
    val clientId: Int,
    val planned_month: Int,
    val planned_year: Int,
    val actual_date: Int,
    val status: String,
    val work_type: String,
    val price: Int,
    val parts: String,
    val comment: String,
    val created_at: Int,
    val updated_at: Int,
)
