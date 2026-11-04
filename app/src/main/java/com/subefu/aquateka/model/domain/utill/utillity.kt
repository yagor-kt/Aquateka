package com.subefu.aquateka.model.domain.utill

import com.subefu.aquateka.App
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.model.Visit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

fun tryExecuteOrSendErrorMessage(
    errorMessage: String,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    action: suspend () -> Unit)
{
    App.applicationScope.launch(dispatcher) {
        try{
            action.invoke()
        }
        catch (e: Exception){
            if (e is CancellationException) throw e
            AppEventBus.post(AppMessage.Error(errorMessage + e.localizedMessage))
        }
    }
}

fun Visit.nextDate() =
    LocalDate.of(this.planned_year, this.planned_month, 1)
        .plusMonths(this.period.toLong())