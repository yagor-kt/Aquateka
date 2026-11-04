package com.subefu.aquateka.model.domain.usecase

import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AddressRepository
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.repository.Repository
import com.subefu.aquateka.model.domain.utill.tryExecuteOrSendErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetAddressVisitUseCase(
    private val repository: Repository,
    private var addressRepository: AddressRepository
) {

    fun execute(visit: Visit) {
        tryExecuteOrSendErrorMessage("Не удалось определить адрес визита. "){
            val address = addressRepository.getAddressFromCoordinates(
                visit.latitude,
                visit.longitude
            ) ?: "-"

            withContext(Dispatchers.IO) {
                repository.updateVisit(visit.copy(address = address))
            }

            AppEventBus.post(AppMessage.Success("Адрес визита определен"))
        }
    }
}
