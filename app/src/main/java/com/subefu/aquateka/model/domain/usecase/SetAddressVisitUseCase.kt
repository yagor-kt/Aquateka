package com.subefu.aquateka.model.domain.usecase

import android.util.Log
import com.subefu.aquateka.App
import com.subefu.aquateka.model.data.repository.AddressRepository
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.repository.Repository
import com.subefu.aquateka.model.domain.utill.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetAddressVisitUseCase(
    private val repository: Repository,
    private var addressRepository: AddressRepository
) {
    // Используем Scope на базе Dispatchers.Main.immediate для работы с Яндексом
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun execute(visit: Visit) {
        Log.d("MyUseCaseVisit", "поиск адреса визита(${visit.hashCode()})...")

        addressRepository.getAddressFromCoordinates(visit.latitude, visit.longitude) { address ->

            // Этот блок кода выполнится ТОЛЬКО тогда, когда Яндекс вернет ответ.
            // Даже если это произойдет через 5 минут или при следующем открытии приложения!
            applicationScope.launch {
                try {
                    val finalAddress = address ?: "-"
                    Log.d("MyUseCaseVisit", "Колбэк сработал. Адрес: $finalAddress. Запись в БД...")

                    // Переключаемся на фоновый поток строго для записи в БД
                    val updateVisit = visit.copy(address = finalAddress)
                    repository.updateVisit(updateVisit)
                    Log.d("MyUseCaseVisit", "...БД обновлена")


                    Log.d("MyUseCaseVisit", "...адрес визита(${visit.hashCode()}) обновлен")
                } catch (e: Exception) {
                    Log.d("MyUseCaseVisit", "Ошибка записи в БД внутри колбэка: ${e.message}")
                }
            }
        }

        Log.d("MyUseCaseVisit", "Метод execute завершен, поток свободен, await() больше ничего не блокирует.")
    }
}
