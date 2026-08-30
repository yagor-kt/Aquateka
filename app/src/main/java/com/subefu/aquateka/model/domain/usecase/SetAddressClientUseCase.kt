package com.subefu.aquateka.model.domain.usecase

import android.util.Log
import com.subefu.aquateka.App
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AddressRepository
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SetAddressClientUseCase(
    private val repository: Repository,
    private var addressRepository: AddressRepository
) {
    // Используем Scope на базе Dispatchers.Main.immediate для работы с Яндексом

    fun execute(client: Client) {
        Log.d("MyUseCaseClient", "поиск адреса клиента(${client.hashCode()})...")

        // Вызываем метод Яндекса. Он мгновенно регистрируется на Главном потоке
        addressRepository.getAddressFromCoordinates(client.latitude, client.longitude) { address ->

            // Этот блок кода выполнится ТОЛЬКО тогда, когда Яндекс вернет ответ.
            // Даже если это произойдет через 5 минут или при следующем открытии приложения!
            App.applicationScope.launch(Dispatchers.Main.immediate) {
                try {
                    val finalAddress = address ?: "-"
                    Log.d("MyUseCaseClient", "Колбэк сработал. Адрес: $finalAddress. Запись в БД...")

                    // Переключаемся на фоновый поток строго для записи в БД
                    val updateClient = client.copy(address = finalAddress)
                    repository.updateClient(updateClient)

                    Log.d("MyUseCaseClient", "...адрес клиента(${client.hashCode()}) обновлен")
                    AppEventBus.post(AppMessage.Success("Адрес клиента определен"))
                } catch (e: Exception) {
                    AppEventBus.post(AppMessage.Error("Не удалось определить адрес клиента"))
                    Log.d("MyUseCaseClient", "Ошибка записи в БД внутри колбэка: ${e.message}")
                }
            }
        }
    }
}