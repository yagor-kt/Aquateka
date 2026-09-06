package com.subefu.aquateka.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.collection.intSetOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subefu.aquateka.App
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AddressRepository
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.model.domain.repository.Repository
import com.subefu.aquateka.model.domain.usecase.PostponeVisitUseCase
import com.subefu.aquateka.model.domain.usecase.SetAddressClientUseCase
import com.subefu.aquateka.model.domain.usecase.SetAddressVisitUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cache
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class EditItemViewModel(
    private val repository: Repository,
    private val setAddressVisitUseCase: SetAddressVisitUseCase,
    private val setAddressClientUseCase: SetAddressClientUseCase,
): ViewModel() {

    val clients: StateFlow<List<Client>> = repository
        .getClients()
        .distinctUntilChanged()
        .catch { e ->
            emit(emptyList())
            postEvent(("Ошибка загрузки клиентов: ${e.localizedMessage}"), true)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private var clientId = MutableStateFlow<Int?>(null)

    val visits: StateFlow<List<Visit>> = clientId
        .filterNotNull()
        .flatMapLatest{ id ->
            repository.getVisitByClient(id)
                .catch { e ->
                    emit(emptyList())
                    postEvent(("Ошибка загрузки визитов: ${e.localizedMessage}"), true)
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val client: StateFlow<Client?> = clientId
        .filterNotNull()
        .flatMapLatest { id ->
            repository.getClientById(id)
        }
        .catch { e ->
            postEvent(("Ошибка загрузки визитов клиента: ${e.localizedMessage}"), true)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setClientId(clientId: Int){
        this.clientId.value = clientId
    }

    fun insertVisit(visit: Visit){
        viewModelScope.launch {
            try {
                Log.d("MyVmEdit", "создаем визит(${visit.hashCode()})...")
                repository.insertVisit(visit)
                Log.d("MyVmEdit", "...визит создан(${visit.hashCode()}) успешно")

                if (visit.address.isNullOrBlank())
                    setAddressVisitUseCase.execute(visit)
                postEvent("Визит создан успешно")
            }catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.d("MyVmEdit", "ошибка в  #insertVisit {${e.message}}")
                postEvent("Не удалось создать визит", true)
            }
        }
    }

    fun insertVisits(visits: List<Visit>){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allIDClients = repository.getAllIDClients()
                val validateVisits = visits.filter { it.clientId in allIDClients }
                repository.insertVisits(validateVisits)
                Log.d("MyEditVM", "insert visits: $visits")
                postEvent("Визиты импортированы", false)
            }catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.d("MyVmEdit", "ошибка в  #insertVisit {${e.message}}")
                postEvent("Не удалось создать визит", true)
            }
        }
    }
    fun insertClients(clients: List<Client>){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertClients(clients)
                postEvent("Клиенты импортированы", false)
            }catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.d("MyVmEdit", "some bag @EditItemViewModel #insertClient {${e.message}}")
                postEvent("Не удалось создать клиента", true)
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

                if (isSearchAddress)
                    setAddressVisitUseCase.execute(visit)

                postEvent("Визит обновлен успешно")
            }catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.d("MyVmEdit", "some bag  #updateVisit {${e.message}}")
                postEvent("Не удалось обновить визит", true)
            }
        }
    }

    fun deleteVisit(visit: Visit){
        viewModelScope.launch {
            try {
                repository.deleteVisit(visit)
                Log.d("MyVmEdit", "визит(${visit.hashCode()}) удален успешно")
                postEvent("Визит удален успешно")
            }catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.d("MyVmEdit", "some bag  #deleteVisit {${e.message}}")
                postEvent("Не удалось удалить визит", true)
            }
        }
    }

    fun insertClient(client: Client){
        viewModelScope.launch {
            try {
                Log.d("MyVmEdit", "добавляем клиента(${client.hashCode()})...")
                val newId = repository.insertClient(client)
                Log.d("MyVmEdit", "...клиент(${client.hashCode()}) добавлен успешно")

                if (client.address.isNullOrBlank())
                    setAddressClientUseCase.execute(client.copy(clietn_id = newId))
                postEvent("Клиент создан успешно")
            }catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.d("MyVmEdit", "some bag @EditItemViewModel #insertClient {${e.message}}")
                postEvent("Не удалось создать клиента", true)
            }
        }
    }

    fun updateClient(client: Client, isSearchAddress: Boolean = false){
        viewModelScope.launch {
            try {
                Log.d("MyVmEdit", "изменяем клиента(${client.hashCode()})...")
                repository.updateClient(client)
                Log.d("MyVmEdit", "...клиент(${client.hashCode()}) изменен успешно")
                if (isSearchAddress)
                    setAddressClientUseCase.execute(client)
                postEvent("Клиент обновлен успешно")
            }catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.d("MyVmEdit", "some bag  #updateClient {${e.message}}")
                postEvent("Не удалось обновить клиента", true)
            }
        }
    }

    fun deleteClient(client: Client){
        viewModelScope.launch {
            try {
                repository.deleteClient(client)
                Log.d("MyVmEdit", "клиент(${client.hashCode()}) удален успешно")
                postEvent("Клиент удален успешно")
            }catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.d("MyVmEdit", "some bag  #deleteClient {${e.message}}")
                postEvent("Не удалось удалить клиента", true)
            }
        }
    }

    fun saveVisit(currentVisit: Visit?, visit: Visit){
        if(currentVisit != null) {
            if (visit.address.isNullOrBlank())
                updateVisit(visit, true)
            else if (visit.address == "-")
                updateVisit(visit.copy(address = ""))
            else
                updateVisit(visit)
        }
        else
            insertVisit(visit)
    }

    fun saveClient(currentClient: Client?, client: Client){
        if(currentClient != null)
            if (client.address.isNullOrBlank())
                updateClient(client, true)
            else if(client.address == "-")
                updateClient(client.copy(address = ""))
            else
                updateClient(client)
        else
            insertClient(client)
    }
    
    fun postEvent(message: String, isError:  Boolean = false){
        if (isError)
            AppEventBus.post(AppMessage.Error(message))
        else
            AppEventBus.post(AppMessage.Success(message))
    }
}
class EditItemViewModelFactory(
    private val repository: Repository,
    ): ViewModelProvider.Factory{
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EditItemViewModel::class.java)){

            val setAddressVisitUseCase = SetAddressVisitUseCase(repository, App.addressRepository)
            val setAddressClientUseCase = SetAddressClientUseCase(repository, App.addressRepository)


            @Suppress("UNCHECKED_CAST")
            return EditItemViewModel(
                repository,
                setAddressVisitUseCase,
                setAddressClientUseCase,
                ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}