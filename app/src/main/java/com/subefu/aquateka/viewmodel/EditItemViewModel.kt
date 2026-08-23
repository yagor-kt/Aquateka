package com.subefu.aquateka.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subefu.aquateka.model.data.repository.AddressRepository
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.repository.Repository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditItemViewModel(private val repository: Repository): ViewModel() {
    private var _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients = _clients.asStateFlow()

    private var _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    init {
        loadClients()
    }

    fun loadClients(){
        viewModelScope.launch {
            try {
                repository.getClients().collect { clients ->
                    _clients.value = clients
                }
            }catch (e: Exception){
                _clients.value = emptyList()
                Log.d("MyLog", "some bag @EditItemViewModel---loadClients {${e.message}}")
            }
        }
    }

    fun insertClient(client: Client){
        viewModelScope.launch {
            try {
                Log.d("MyVM", "визит добавляем")
                repository.insertClient(client)
                Log.d("MyVM", "визит добавился успешно")
            }catch (e: Exception){
                Log.d("MyLog", "some bag @EditItemViewModel---insertClient {${e.message}}")
            }
        }
    }

    fun insertVisit(visit: Visit){
        viewModelScope.launch {
            try {
                Log.d("MyVM", "визит добавляем")
                var visit = visit
                if (visit.address.isNullOrBlank()){
                    val addressRepository = AddressRepository()
                    val address = addressRepository.getAddressFromCoordinates(visit.latitude, visit.longitude)
                    visit = visit.copy(address = address)
                }
                repository.insertVisit(visit)
                Log.d("MyVM", "визит добавился успешно")
            }catch (e: Exception){
                Log.d("MyLog", "some bag @EditItemViewModel---insertClient {${e.message}}")
            }
        }
    }

    fun deleteClient(client: Client){
        viewModelScope.launch {
            try {
                repository.deleteClient(client)
                Log.d("MyVM", "клиент удален успешно")
            }catch (e: Exception){
                Log.d("MyLog", "some bag @EditItemViewModel---deleteClient {${e.message}}")
            }
        }
    }



    fun deleteVisit(visit: Visit){
        viewModelScope.launch {
            try {
                repository.deleteVisit(visit)
                Log.d("MyVM", "визит удален успешно")
            }catch (e: Exception){
                Log.d("MyLog", "some bag @EditItemViewModel---deleteVisit {${e.message}}")
            }
        }
    }

    fun updateVisit(visit: Visit){
        viewModelScope.launch {
            try {
                repository.updateVisit(visit)
                Log.d("MyVM", "визит изменен успешно")
            }catch (e: Exception){
                Log.d("MyLog", "some bag @EditItemViewModel---deleteVisit {${e.message}}")
            }
        }
    }

    fun updateClient(client: Client){
        viewModelScope.launch {
            try {
                repository.updateClient(client)
                Log.d("MyVM", "клиент изменен успешно")
            }catch (e: Exception){
                Log.d("MyLog", "some bag @EditItemViewModel---deleteVisit {${e.message}}")
            }
        }
    }


}
class EditItemViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EditItemViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return EditItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}