package com.subefu.aquateka.model.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Visit(
    val id: Int,
    val clientId: Int,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val planned_day: Int,
    val planned_month: Int,
    val planned_year: Int,
    val actual_date: Int,
    val status: String,
    val work_type: String,
    val price: Int,
    val parts: String,
    val comment: String,
    val period: Int,
    val color: String,
): Parcelable
