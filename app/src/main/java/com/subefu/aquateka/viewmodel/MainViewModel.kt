package com.subefu.aquateka.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.model.domain.repository.Repository
import com.subefu.aquateka.model.domain.usecase.PostponeVisitUseCase
import com.subefu.aquateka.model.domain.utill.ImportExportState
import com.subefu.aquateka.view.utils.UnapprovedVisitsPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(
    private val repository: Repository,
    private val postponeVisitUseCase: PostponeVisitUseCase,
): ViewModel() {

    private val currantDate: MutableStateFlow<Pair<Int, Int>>
        = MutableStateFlow(Pair(LocalDate.now().monthValue, LocalDate.now().year))

    private val currantDateForMap: MutableStateFlow<Pair<Int, Int>>
        = MutableStateFlow(Pair(LocalDate.now().monthValue, LocalDate.now().year))

    private val _parsedVisits =
        MutableStateFlow<List<Visit>>(emptyList())
    val parsedVisits = _parsedVisits.asStateFlow()

    private val _parsedString =
        MutableStateFlow<String?>(null)
    val parsedString = _parsedString.asStateFlow()

    private val _importState = MutableSharedFlow<ImportExportState>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val importState: SharedFlow<ImportExportState> = _importState.asSharedFlow()

    val unapprovedVisits: StateFlow<UnapprovedVisitsPayload?> = currantDate
        .flatMapLatest { date ->
            repository.getVisitBeforeDate(
                listOf(MyConst.PLANNED, MyConst.RESCHEDULE_FROM_PAST),
                date.first,
                date.second
            )
                .map { visits -> UnapprovedVisitsPayload(visits, date.hashCode()) }
        }
        .distinctUntilChanged()
        .catch { e ->
            postEvent(("Ошибка загрузки невыполненных визитов: ${e.localizedMessage}"), true)
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null,)

    val visits: StateFlow<List<VisitWithClient>> = currantDate
        .flatMapLatest { date ->
            repository.getVisitWithClientForMonth(date.first, date.second)
        }
        .retryWhen{ cause, attempt ->
            if (cause is IllegalStateException && cause.message?.contains("Relationship item") == true && attempt < 3){
                delay(100)
                true
            }else false
        }
        .distinctUntilChanged()
        .catch { e ->
            emit(emptyList())
            postEvent(("Ошибка загрузки визитов: ${e.localizedMessage}"), true)
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val visitsOnMap: StateFlow<List<VisitWithClient>> = currantDateForMap
        .flatMapLatest { date ->
            repository.getVisitWithClientForMonth(date.first, date.second)
        }
        .distinctUntilChanged()
        .catch { e ->
            emit(emptyList())
            postEvent(("Ошибка загрузки визитов для карты: ${e.localizedMessage}"), true)
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val clients: StateFlow<List<Client>> = repository.getClients()
        .distinctUntilChanged()
        .catch { e ->
            emit(emptyList())
            postEvent(("Ошибка загрузки клиентов: ${e.localizedMessage}"), true)
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun setCurrentDate(month: Int, year: Int){
        currantDate.value = Pair(month, year)
        Log.d("MyMainVM", "new date: $month.$year")
    }

    fun setCurrentDateForMap(month: Int, year: Int){
        currantDateForMap.value = Pair(month, year)
    }

    suspend fun getVisitsByClientIds(clientIds: List<Int>): List<VisitWithClient>
    = withContext(Dispatchers.IO){
        repository.getVisitsByClientIds(clientIds)
    }

    fun generateCsvData(visits: Collection<VisitWithClient>) {
        viewModelScope.launch(Dispatchers.IO) {
            val sb = StringBuilder()
            // 1. Заголовок таблицы (15 колонок)
            sb.append("id;clientId;address;latitude;longitude;clientName;plannedMonth;plannedYear;actualDate;status;workType;price;parts;period;comment\n")
            // 2. Заполнение данными (динамически экранируем точки с запятой, если они есть в комментариях)
            for (v in visits) {
                sb.append("${v.visit.id ?: ""};")
                sb.append("${v.visit.clientId};")
                sb.append("${(v.visit.address ?: "").replace(";", ",")};")
                sb.append("${v.visit.latitude};")
                sb.append("${v.visit.longitude};")
                sb.append("${v.client.name.replace(";", ",")};") // Подставляем имя из связанной сущности
                sb.append("${v.visit.planned_month};")
                sb.append("${v.visit.planned_year};")
                sb.append("${v.visit.actual_date};")
                sb.append("${v.visit.status};")
                sb.append("${v.visit.work_type};")
                sb.append("${v.visit.price};")
                sb.append("${v.visit.parts.replace(";", ",")};")
                sb.append("${v.visit.period};")
                sb.append("${v.visit.comment.replace(";", ",")}\n") // Обязательный перенос строки \n
            }
            _parsedString.value = sb.toString()
            _importState.emit(ImportExportState.SuccessExportVisits(sb.toString()))
        }
    }

    fun generateCsvData(clients: List<Client>) {
        viewModelScope.launch(Dispatchers.IO) {
            val sb = StringBuilder()
            // 1. Заголовок таблицы
            sb.append("clietn_id;name;phone;address;latitude;longitude;period_month;comment\n")
            // 2. Заполнение данными (динамически экранируем точки с запятой, если они есть в комментариях)
            for (c in clients) {
                sb.append("${c.clietn_id ?: ""};")
                sb.append("${c.name};")
                sb.append("${c.phone};")
                sb.append("${(c.address ?: "").replace(";", ",")};")
                sb.append("${c.latitude};")
                sb.append("${c.longitude};")
                sb.append("${c.period_month};")
                sb.append("${c.comment.replace(";", ",")}\n")
            }
            _importState.emit(ImportExportState.SuccessExportClients(sb.toString()))
        }
    }

    fun parseCsvToVisits(csvContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val visits = mutableListOf<Visit>()

            val lines = csvContent.split("\n")
            if (lines.isEmpty()) return@launch

            // Пропускаем заголовок
            if (!lines.first().contains("id;clientId;address;latitude;longitude;", true)) {
                _importState.emit(ImportExportState.Error("Ошибка импорта визитов: некорректный формат"))
                return@launch
            }
            val dataLines = lines.drop(1).filter { it.isNotBlank() }

            for (line in dataLines) {
                val values = line.split(";").map { it.trim() }
                if (values.size < 15) continue

                try {
                    val visit = Visit(
                        id = values[0].toIntOrNull() ?: 0, // Новые записи получат автоинкремент
                        clientId = values[1].toIntOrNull() ?: continue,
                        address = values[2].ifEmpty { null },
                        latitude = values[3].toDoubleOrNull() ?: 0.0,
                        longitude = values[4].toDoubleOrNull() ?: 0.0,
                        planned_month = values[6].toIntOrNull() ?: 1,
                        planned_year = values[7].toIntOrNull() ?: 2026,
                        actual_date = values[8].toIntOrNull() ?: 0,
                        status = values[9],
                        work_type = values[10],
                        price = values[11].toIntOrNull() ?: 0,
                        parts = values[12],
                        period = values[13].toIntOrNull() ?: 0,
                        comment = values[14]
                    )

                    visits.add(visit)
                } catch (e: Exception) {
                    AppEventBus.post(AppMessage.Error("Ошибка парсинга строки: ${e.message}"))
                }
            }

            _parsedVisits.value = visits.toList()
            _importState.emit(ImportExportState.SuccessImportVisits(visits))
        }
    }

    fun parseCsvToClients(csvContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val clients = mutableListOf<Client>()

            val lines = csvContent.split("\n")
            if (lines.isEmpty()) return@launch

            // Пропускаем заголовок
            if (!lines.first().contains("clietn_id;name;phone;address;", true)) {
                _importState.emit(ImportExportState.Error("Ошибка импорта клиентов: некорректный формат"))
                return@launch
            }
            val dataLines = lines.drop(1).filter { it.isNotBlank() }

            for (line in dataLines) {
                val values = line.split(";").map { it.trim() }
                if (values.size < 8) continue

                try {
                    val client = Client(
                        clietn_id = values[0].toIntOrNull() ?: 0, // Новые записи получат автоинкремент
                        name = values[1],
                        phone = values[2],
                        address = values[3],
                        latitude = values[4].toDoubleOrNull() ?: 0.0,
                        longitude = values[5].toDoubleOrNull() ?: 0.0,
                        period_month = values[6].toIntOrNull() ?: 1,
                        comment = values[7]
                    )

                    clients.add(client)
                } catch (e: Exception) {
                    AppEventBus.post(AppMessage.Error("Ошибка парсинга строки: ${e.message}"))
                }
            }

            _importState.emit(ImportExportState.SuccessImportClients(clients))
        }
    }

    fun postponeVisit(
        date: Pair<Int, Int>? = null,
        mode: String,
        vararg visit: Visit,
    ) {
        Log.d("MyMainVM", "postpone visits: ${visit.toList()}, on date: $date")
        postponeVisitUseCase.execute(visit.toList(), mode, date ?: currantDate.value)
    }

    fun resetImportState() {
        viewModelScope.launch {
            _importState.emit(ImportExportState.Idle)
        }
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
            val postponeVisitUseCase = PostponeVisitUseCase(repository)

            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                repository,
                postponeVisitUseCase,
                ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}