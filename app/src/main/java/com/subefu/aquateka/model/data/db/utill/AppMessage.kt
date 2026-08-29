package com.subefu.aquateka.model.data.db.utill

sealed class AppMessage {
    data class Success(val text: String) : AppMessage()
    data class Error(val text: String) : AppMessage()
    data class TouchMapPoint(val text: String, val item: Any) : AppMessage()
    object None: AppMessage()
}