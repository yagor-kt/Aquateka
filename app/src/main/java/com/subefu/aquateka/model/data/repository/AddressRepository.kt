package com.subefu.aquateka.model.data.repository

import android.util.Log
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import kotlinx.coroutines.Deferred


class AddressRepository {
    private val searchManager: SearchManager = SearchFactory.getInstance()
        .createSearchManager(SearchManagerType.ONLINE)

    // Держим ссылку на сессию на уровне репозитория, чтобы GC её не удалил
    private var searchSession: Session? = null

    // Функция принимает callback, который отработает по завершении
    fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        Log.d("MyYandex", "Запуск прямого запроса в Яндекс...")
        val targetPoint = Point(latitude, longitude)
        val searchOptions = SearchOptions().apply {
            searchTypes = SearchType.GEO.value
            resultPageSize = 1
        }

        searchSession = searchManager.submit(
            targetPoint,
            null,
            searchOptions,
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val geoObject: GeoObject? = response.collection.children.firstOrNull()?.obj
                    val addressText: String? = geoObject?.name ?: geoObject?.metadataContainer
                        ?.getItem(com.yandex.mapkit.search.ToponymObjectMetadata::class.java)
                        ?.address
                        ?.formattedAddress

                    Log.d("MyYandex", "Яндекс ответил успехом: $addressText")
                    onResult(addressText)
                }

                override fun onSearchError(error: com.yandex.runtime.Error) {
                    Log.d("MyYandex", "Яндекс ответил ошибкой: $error")
                    onResult(null)
                }
            }
        )
    }
}
