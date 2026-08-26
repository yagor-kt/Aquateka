package com.subefu.aquateka

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    companion object{
        val globalScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.MAP_API_kEY)
        MapKitFactory.initialize(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}