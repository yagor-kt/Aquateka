package com.subefu.aquateka.model.domain.usecase

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.subefu.aquateka.App
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.repository.Repository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class PostponeVisitUseCase(private val repository: Repository) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun execute(visits: List<Visit>, mode: String, date: Pair<Int, Int>?){
        if(visits.isEmpty()){
            AppEventBus.post(AppMessage.Error("Передан пустой список"))
            return
        }
        if (mode == MyConst.APPROVE)
            approveVisit(visits[0])
        else if (mode == MyConst.MANUAL_POSTPONE || mode == MyConst.AUTOMATIC_POSTPONE)
            postponeVisit(visits, date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun approveVisit(visit: Visit){
        Log.d("MyPostponeUseCase", "starting approve visit: $visit")
        App.applicationScope.launch(Dispatchers.IO) {
            try {
                repository.updateVisit(
                    visit.copy(
                        status = MyConst.COMPLETED,
                        actual_date = getCurrentDate()
                    )
                )
                repository.insertVisit(
                    visit.copy(
                        id = 0,
                        planned_year = getNextDate(visit).year,
                        planned_month = getNextDate(visit).monthValue,
                        status = MyConst.PLANNED
                    )
                )
                AppEventBus.post(AppMessage.Success("Визит завешен успешно"))
            }catch (e: Exception){
                if (e is CancellationException) throw e
                AppEventBus.post(AppMessage.Error("Ошибка при завершении визита: ${e.localizedMessage}"))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postponeVisit(visits: List<Visit>, date: Pair<Int, Int>?){
        Log.d("MyPostponeUseCase", "starting postpone visits: $visits")
        if (date == null){
            AppEventBus.post(AppMessage.Error("Ошибка переноса визита: дата не выбрана"))
            return
        }

        val updateCurrentVisit: suspend (visit: Visit) -> Unit = { visit ->
            repository.updateVisit(
                visit.copy(
                    status = MyConst.POSTPONED,
                    comment = visit.comment + " /// Отложен на ${date.first}.${date.second};"
                )
            )
        }
        val createNewVisit: suspend (visit: Visit) -> Unit = { visit ->
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

        val postponeVisit: suspend (visits: List<Visit>) -> Unit ={ visits ->
            try {
                visits.forEach {  visit ->
                    Log.d("MyPostponeUseCase", "starting postpone is visit: $visit")
                    updateCurrentVisit(visit)
                    createNewVisit(visit)
                }
                if (visits.size > 1) AppEventBus.post(AppMessage.Success("Визиты перенесены успешно"))
                else AppEventBus.post(AppMessage.Success("Визит перенесен успешно"))
            }catch (e: Exception){
                if (e is CancellationException) throw e
                AppEventBus.post(AppMessage.Error("Ошибка при завершении визита: ${e.localizedMessage}"))
            }
        }
            App.applicationScope.launch(Dispatchers.IO) {
                postponeVisit(visits)
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDate(): Int {
        val date = LocalDate.now().toEpochDay()
        return date.toInt()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNextDate(visit: Visit) =
        LocalDate.of(visit.planned_year, visit.planned_month, 1)
            .plusMonths(visit.period.toLong())
}