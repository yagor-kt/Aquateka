package com.subefu.aquateka.model.domain.usecase

import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.utill.MyConst
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.repository.Repository
import com.subefu.aquateka.model.domain.utill.nextDate
import com.subefu.aquateka.model.domain.utill.tryExecuteOrSendErrorMessage
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate

class RescheduleVisitUseCase(private val repository: Repository) {

    val currentDay = LocalDate.now().toEpochDay().toInt()

    fun execute(visits: List<Visit>, mode: String, date: Pair<Int, Int>?){
        if(visits.isEmpty()){
            AppEventBus.post(AppMessage.Error("Передан пустой список"))
            return
        }

        if (mode == MyConst.APPROVE)
            approveAndRescheduleVisit(visits.first())
        else if (mode == MyConst.MANUAL_POSTPONE || mode == MyConst.AUTOMATIC_POSTPONE)
            rescheduleVisitsToDate(visits, date)
    }

    private fun approveAndRescheduleVisit(visit: Visit){
        tryExecuteOrSendErrorMessage("Ошибка при завершении визита. ", Dispatchers.IO) {
            repository.updateVisit(
                visit.copy(
                    status = MyConst.COMPLETED,
                    actual_date = currentDay
                )
            )
            repository.insertVisit(
                visit.copy(
                    id = 0,
                    planned_year = visit.nextDate().year,
                    planned_month = visit.nextDate().monthValue,
                    status = MyConst.PLANNED
                )
            )
            AppEventBus.post(AppMessage.Success("Визит завешен успешно"))
        }
    }

    private fun rescheduleVisitsToDate(visits: List<Visit>, date: Pair<Int, Int>?){
        if (date == null){
            AppEventBus.post(AppMessage.Error("Ошибка переноса визита: дата не выбрана"))
            return
        }

        tryExecuteOrSendErrorMessage("Ошибка при завершении визита. ", Dispatchers.IO){
            visits.forEach {  visit ->
                updateVisit(visit, date)
                insertNewVisit(visit, date)
            }
            if (visits.size > 1) AppEventBus.post(AppMessage.Success("Визиты перенесены успешно"))
            else AppEventBus.post(AppMessage.Success("Визит перенесен успешно"))
        }
    }

    private suspend fun updateVisit(visit: Visit, date: Pair<Int, Int>) {
        repository.updateVisit(
            visit.copy(
                status = MyConst.POSTPONED,
                comment = visit.comment + " /// Отложен на ${date.first}.${date.second};"
            )
        )
    }

    private suspend fun insertNewVisit(visit: Visit, date: Pair<Int, Int>) {
        repository.insertVisit(
            visit.copy(
                id = 0,
                planned_month = date.first,
                planned_year = date.second,
                status = MyConst.RESCHEDULE_FROM_PAST,
                comment = visit.comment + " /// Перенесен c ${visit.planned_month}.${visit.planned_year};"
            )
        )
    }
}