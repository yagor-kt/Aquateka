package com.subefu.aquateka.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.model.domain.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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