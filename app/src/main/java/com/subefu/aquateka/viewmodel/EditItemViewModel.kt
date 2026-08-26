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
    private val setAddressVisitUseCase: SetAddressVisitUseCase? = null
): ViewModel() {
    private var _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients = _clients.asStateFlow()

    private var _events = MutableSharedFlow<Result<Visit>>()
    val events = _events.asSharedFlow()

    private var addressRepository: AddressRepository? = null

    init {
        loadClients()
        addressRepository = AddressRepository()
        setAddressVisitUseCase?.let { it.setAddressRepository(addressRepository!!) }
    }

    fun loadClients(){
        viewModelScope.launch {
            try {
                Log.d("MyVM", "клиенты загружены #EditItemViewModel #loadClients")
                repository.getClients().collect { clients ->
                    _clients.value = clients
                }
            }catch (e: Exception){
                _clients.value = emptyList()
                Log.d("MyVM", "some bag #EditItemViewModel #loadClients {${e.message}}")
            }
        }
    }

    fun insertVisit(visit: Visit){
        viewModelScope.launch {
            try {
                Log.d("MyVM", "создаем визит...")
                repository.insertVisit(visit)
                Log.d("MyVM", "...визит создан успешно")

                if (visit.address == ""){
                    setAddressVisitUseCase?.execute(visit)
                }
            }catch (e: Exception){
                Log.d("MyVM", "ошибка в #EditItemViewModel #insertVisit {${e.message}}")
            }
        }
    }

    //обновляем с поиском адреса по условию
    fun updateVisit(visit: Visit, isSearchAddress: Boolean = false){
        viewModelScope.launch {
            try {
                Log.d("MyVM", "визит изменяем")
                repository.updateVisit(visit)
                Log.d("MyVM", "визит изменен успешно")

                if (isSearchAddress){
                    setAddressVisitUseCase?.execute(visit)
                }
            }catch (e: Exception){
                Log.d("MyVM", "some bag #EditItemViewModel #updateVisit {${e.message}}")
            }
        }
    }

    fun deleteVisit(visit: Visit){
        viewModelScope.launch {
            try {
                repository.deleteVisit(visit)
                Log.d("MyVM", "визит удален успешно")
            }catch (e: Exception){
                Log.d("MyVM", "some bag #EditItemViewModel #deleteVisit {${e.message}}")
            }
        }
    }

    fun insertClient(client: Client){
        viewModelScope.launch {
            try {
                Log.d("MyVM", "визит добавляем")
                repository.insertClient(client)

//                if (client.address.isNullOrBlank()){
//                    val address = addressRepository?.getAddressFromCoordinates(
//                        client.latitude, client.longitude
//                    )
//                    val updateClient = client.copy(address = address)
//                    repository.updateClient(updateClient)
//                }
                Log.d("MyVM", "визит добавился успешно")
            }catch (e: Exception){
                Log.d("MyVM", "some bag @EditItemViewModel #insertClient {${e.message}}")
            }
        }
    }

    fun updateClient(client: Client, isSearchAddress: Boolean = false){
        viewModelScope.launch {
            try {
                Log.d("MyVM", "клиент изменяем")
                repository.updateClient(client)
                if (isSearchAddress){
//                    val address = addressRepository?.getAddressFromCoordinates(
//                        client.latitude, client.longitude
//                    ) ?: ""
//                    val updateClient = client.copy(address = address)
//                    repository.updateClient(updateClient)
                }
                Log.d("MyVM", "клиент изменен успешно")
            }catch (e: Exception){
                Log.d("MyVM", "some bag #EditItemViewModel #updateClient {${e.message}}")
            }
        }
    }

    fun deleteClient(client: Client){
        viewModelScope.launch {
            try {
                repository.deleteClient(client)
                Log.d("MyVM", "клиент удален успешно")
            }catch (e: Exception){
                Log.d("MyVM", "some bag #EditItemViewModel #deleteClient {${e.message}}")
            }
        }
    }
}
class EditItemViewModelFactory(
    private val repository: Repository,
    private val setAddressVisitUseCase: SetAddressVisitUseCase? = null
    ): ViewModelProvider.Factory{
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EditItemViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return EditItemViewModel(repository, setAddressVisitUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}