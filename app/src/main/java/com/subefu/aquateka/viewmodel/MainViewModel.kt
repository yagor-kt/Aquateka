package com.subefu.aquateka.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.model.domain.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(private val repository: Repository): ViewModel() {
    private val _visits
        = MutableStateFlow<List<VisitWithClient>>(emptyList())
    val visits: StateFlow<List<VisitWithClient>> = _visits.asStateFlow()

    init {
        val currantDay = LocalDate.now()
        loadVisits(currantDay.monthValue, currantDay.year)
    }

    fun loadVisits(month: Int, year: Int){
        viewModelScope.launch {
            try {
                repository.getVisitWithClientForMonth(month, year).collect{ visits ->
                    _visits.value = visits
                }
            }catch (e: Exception){
                _visits.value = emptyList()
                Log.d("MyLog", "some bag @loadOrders {${e.message}}")
            }
        }
    }

    fun generateCsvData(visits: List<VisitWithClient>): String {
        val sb = StringBuilder()
        // Заголовок таблицы (колонки должны строго соответствовать вашей Entity для импорта)
        sb.append("id;clientName;plannedMonth;plannedYear;actualDate;status;workType;price;parts;comment;createdAt;updatedAt\n")

        // Заполнение данными
        for (v in visits) {
            sb.append("${v.visit.id};${v.client.name};${v.visit.planned_month};")
            sb.append("${v.visit.planned_year};${v.visit.actual_date};${v.visit.status};${v.visit.work_type};")
            sb.append("${v.visit.price};${v.visit.parts};${v.visit.comment};${v.visit.created_at};${v.visit.updated_at}\n")
        }
        return sb.toString()
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