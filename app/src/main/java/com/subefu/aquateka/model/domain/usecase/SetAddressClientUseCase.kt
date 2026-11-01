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
        addressRepository.getAddressFromCoordinates(client.latitude, client.longitude) { address ->
            tryExecuteOrSendErrorMessage("Не удалось определить адрес клиента. ") {
                val finalAddress = address ?: "-"
                val updateClient = client.copy(address = finalAddress)

                withContext(Dispatchers.IO) {
                    repository.updateClient(updateClient)
                }

                AppEventBus.post(AppMessage.Success("Адрес клиента определен"))
            }
        }
    }
}