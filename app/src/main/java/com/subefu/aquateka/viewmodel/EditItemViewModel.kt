package com.subefu.aquateka.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subefu.aquateka.App
import com.subefu.aquateka.model.data.repository.AddressRepository
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.repository.Repository
import com.subefu.aquateka.model.domain.usecase.SetAddressClientUseCase
import com.subefu.aquateka.model.domain.usecase.SetAddressVisitUseCase
import com.subefu.aquateka.model.domain.utill.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class EditItemViewModel(
    private val repository: Repository,
    private val setAddressVisitUseCase: SetAddressVisitUseCase? = null,
    private val setAddressClientUseCase: SetAddressClientUseCase? = null,
): ViewModel() {
    private var _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients = _clients.asStateFlow()

    private var addressRepository: AddressRepository? = null

    init {
        loadClients()
        addressRepository = AddressRepository()
        setAddressVisitUseCase?.let { it.setAddressRepository(addressRepository!!) }
        setAddressClientUseCase?.let { it.setAddressRepository(addressRepository!!) }
    }

    fun loadClients(){
        viewModelScope.launch {
            try {
                Log.d("MyVmEdit", "клиенты загружены  #loadClients")
                repository.getClients().collect { clients ->
                    _clients.value = clients
                }
            }catch (e: Exception){
                _clients.value = emptyList()
                Log.d("MyVmEdit", "some bag  #loadClients {${e.message}}")
            }
        }
    }

    fun insertVisit(visit: Visit){
        viewModelScope.launch {
            try {
                Log.d("MyVmEdit", "создаем визит(${visit.hashCode()})...")
                repository.insertVisit(visit)
                Log.d("MyVmEdit", "...визит создан(${visit.hashCode()}) успешно")

                if (visit.address.isNullOrBlank()){
                    setAddressVisitUseCase?.execute(visit)
                }
            }catch (e: Exception){
                Log.d("MyVmEdit", "ошибка в  #insertVisit {${e.message}}")
            }
        }
    }

    //обновляем с поиском адреса по условию
    fun updateVisit(visit: Visit, isSearchAddress: Boolean = false){
        viewModelScope.launch {
            try {
                Log.d("MyVmEdit", "изменяем визит(${visit.hashCode()})...")
                repository.updateVisit(visit)
                Log.d("MyVmEdit", "...визит(${visit.hashCode()}) изменен успешно")

                if (isSearchAddress){
                    setAddressVisitUseCase?.execute(visit)
                }
            }catch (e: Exception){
                Log.d("MyVmEdit", "some bag  #updateVisit {${e.message}}")
            }
        }
    }

    fun deleteVisit(visit: Visit){
        viewModelScope.launch {
            try {
                repository.deleteVisit(visit)
                Log.d("MyVmEdit", "визит(${visit.hashCode()}) удален успешно")
            }catch (e: Exception){
                Log.d("MyVmEdit", "some bag  #deleteVisit {${e.message}}")
            }
        }
    }

    fun insertClient(client: Client){
        viewModelScope.launch {
            try {
                Log.d("MyVmEdit", "добавляем клиента(${client.hashCode()})...")
                repository.insertClient(client)
                Log.d("MyVmEdit", "...клиент(${client.hashCode()}) добавлен успешно")

                if (client.address.isNullOrBlank()){
                    setAddressClientUseCase?.execute(client)
                }
            }catch (e: Exception){
                Log.d("MyVmEdit", "some bag @EditItemViewModel #insertClient {${e.message}}")
            }
        }
    }

    fun updateClient(client: Client, isSearchAddress: Boolean = false){
        viewModelScope.launch {
            try {
                Log.d("MyVmEdit", "изменяем клиента(${client.hashCode()})...")
                repository.updateClient(client)
                Log.d("MyVmEdit", "...клиент(${client.hashCode()}) изменен успешно")
                if (isSearchAddress){
                    setAddressClientUseCase?.execute(client)
                }
            }catch (e: Exception){
                Log.d("MyVmEdit", "some bag  #updateClient {${e.message}}")
            }
        }
    }

    fun deleteClient(client: Client){
        viewModelScope.launch {
            try {
                repository.deleteClient(client)
                Log.d("MyVmEdit", "клиент(${client.hashCode()}) удален успешно")
            }catch (e: Exception){
                Log.d("MyVmEdit", "some bag  #deleteClient {${e.message}}")
            }
        }
    }
}
class EditItemViewModelFactory(
    private val repository: Repository,
    private val setAddressVisitUseCase: SetAddressVisitUseCase? = null,
    private val setAddressClientUseCase: SetAddressClientUseCase? = null,
    ): ViewModelProvider.Factory{
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EditItemViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return EditItemViewModel(repository, setAddressVisitUseCase, setAddressClientUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}