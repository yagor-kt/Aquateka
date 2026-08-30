package com.subefu.aquateka.view.utils

import com.subefu.aquateka.model.domain.model.Visit

data class UnapprovedVisitsPayload(
    val visits: List<Visit>,
    val dateHash: Int // Хэш или маркер даты, которая вызвала запрос
)