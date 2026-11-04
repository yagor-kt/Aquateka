package com.subefu.aquateka.model.domain.usecase

import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AddressRepository
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.repository.Repository
import com.subefu.aquateka.model.domain.utill.tryExecuteOrSendErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetAddressClientUseCase(
    private val repository: Repository,
    private var addressRepository: AddressRepository
) {

    fun execute(client: Client) {
        tryExecuteOrSendErrorMessage("Не удалось определить адрес клиента. "){
            val address = addressRepository.getAddressFromCoordinates(
                client.latitude,
                client.longitude
            ) ?: "-"

            withContext(Dispatchers.IO) {
                repository.updateClient(client.copy(address = address))
            }

            AppEventBus.post(AppMessage.Success("Адрес клиента определен"))
        }
    }
}