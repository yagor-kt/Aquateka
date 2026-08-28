package com.subefu.aquateka.model.data.repository

import android.util.Log
import com.subefu.aquateka.model.data.db.utill.AppMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow

object AppEventBus {
    private val _events = Channel<AppMessage>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun post(message: AppMessage){
        Log.d("MyEventBus", "post: ${message}")
        _events.trySend(message)
    }
}