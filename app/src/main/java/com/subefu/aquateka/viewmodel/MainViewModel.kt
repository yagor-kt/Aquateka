package com.subefu.aquateka.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.model.domain.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(private val repository: Repository): ViewModel() {

    private val currantDate: MutableStateFlow<Pair<Int, Int>>
        = MutableStateFlow(Pair(LocalDate.now().monthValue, LocalDate.now().year))

    private val currantDateForMap: MutableStateFlow<Pair<Int, Int>>
        = MutableStateFlow(Pair(LocalDate.now().monthValue, LocalDate.now().year))

    val visits: StateFlow<List<VisitWithClient>> = currantDate
        .flatMapLatest { date ->
            repository.getVisitWithClientForMonth(date.first, date.second)
        }
        .distinctUntilChanged()
        .catch { e ->
            emit(emptyList())
            postEvent(("Ошибка загрузки визитов: ${e.localizedMessage}"), true)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val visitsOnMap: StateFlow<List<VisitWithClient>> = currantDateForMap
        .flatMapLatest { date ->
            repository.getVisitWithClientForMonth(date.first, date.second)
        }
        .distinctUntilChanged()
        .catch { e ->
            emit(emptyList())
            postEvent(("Ошибка загрузки визитов для карты: ${e.localizedMessage}"), true)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val clients: StateFlow<List<Client>> = repository.getClients()
        .distinctUntilChanged()
        .catch { e ->
            emit(emptyList())
            postEvent(("Ошибка загрузки клиентов: ${e.localizedMessage}"), true)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setCurrentDate(month: Int, year: Int){
        currantDate.value = Pair(month, year)
    }

    fun setCurrentDateForMap(month: Int, year: Int){
        currantDateForMap.value = Pair(month, year)
    }

    fun generateCsvData(visits: List<VisitWithClient>): String {
        val sb = StringBuilder()
        // Заголовок таблицы (колонки должны строго соответствовать вашей Entity для импорта)
        sb.append("id;address;latitude;longitude;clientName;plannedMonth;plannedYear;actualDate;status;workType;price;parts;comment;createdAt;updatedAt\n")

        // Заполнение данными
        for (v in visits) {
            sb.append("${v.visit.id};${v.visit.address};${v.visit.latitude};${v.visit.longitude};${v.visit.planned_month};")
            sb.append("${v.visit.planned_year};${v.visit.actual_date};${v.visit.status};${v.visit.work_type};")
            sb.append("${v.visit.price};${v.visit.parts};${v.visit.comment}")
        }
        return sb.toString()
    }

    fun postEvent(message: String, isError:  Boolean = false){
        if (isError)
            AppEventBus.post(AppMessage.Error(message))
        else
            AppEventBus.post(AppMessage.Success(message))
    }
}

class MainViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}