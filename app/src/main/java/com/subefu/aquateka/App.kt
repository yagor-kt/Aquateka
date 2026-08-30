package com.subefu.aquateka

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.AddressRepository
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.repository.Repository
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    companion object{
        lateinit var repository : Repository
        lateinit var addressRepository: AddressRepository
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.MAP_API_kEY)
        MapKitFactory.initialize(this)

        repository = RepositoryImpl(DataBase.getDB(baseContext).getDao())
        addressRepository = AddressRepository()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}