package com.subefu.aquateka.model.domain.usecase

import android.util.Log
import com.subefu.aquateka.App
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AddressRepository
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.repository.Repository
import com.subefu.aquateka.model.domain.utill.tryExecuteOrSendErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetAddressVisitUseCase(
    private val repository: Repository,
    private var addressRepository: AddressRepository
) {

    fun execute(visit: Visit) {
        addressRepository.getAddressFromCoordinates(visit.latitude, visit.longitude) { address ->
            tryExecuteOrSendErrorMessage("Ошибка записи в БД внутри колбэка. "){
                val finalAddress = address ?: "-"
                val updateVisit = visit.copy(address = finalAddress)

                withContext(Dispatchers.IO){
                    repository.updateVisit(updateVisit)
                }

                AppEventBus.post(AppMessage.Success("Адрес визита определен"))
            }
        }
    }
}
