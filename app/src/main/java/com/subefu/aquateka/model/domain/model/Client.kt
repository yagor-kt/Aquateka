package com.subefu.aquateka.model.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Client(
    val cllietn_id: Int,
    val name: String,
    val phone: String,
    val address: String?,
    val latitude: Float,
    val longitude: Float,
    val period_month: Int?,
    val comment: String
): Parcelable