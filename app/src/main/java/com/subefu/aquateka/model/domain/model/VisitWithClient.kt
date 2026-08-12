package com.subefu.aquateka.model.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VisitWithClient(
    val visit: Visit,
    val client: Client
): Parcelable