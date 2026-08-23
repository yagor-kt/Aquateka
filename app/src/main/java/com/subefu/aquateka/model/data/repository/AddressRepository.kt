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
import kotlinx.coroutines.suspendCancellableCoroutine


class AddressRepository {

    // Инициализируем SearchManager через фабрику
    private val searchManager: SearchManager = SearchFactory.getInstance()
        .createSearchManager(SearchManagerType.ONLINE)

    private var searchSession: Session? = null

    suspend fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double
    ): String? = suspendCancellableCoroutine { continuation ->

        Log.d("MyYandex", "request fun getAddressFromCoordinates")
        val targetPoint = Point(latitude, longitude)
        val searchOptions = SearchOptions().apply {
            searchTypes = SearchType.GEO.value
            resultPageSize = 1
        }
        Log.d("MyYandex", "targetPoint: $targetPoint \nsearchOptions: $searchOptions")
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

                    Log.d("MyYandex", "Успех геокодирования: $addressText")
                    if (continuation.isActive) continuation.resumeWith(Result.success(addressText))
                }

                override fun onSearchError(p0: com.yandex.runtime.Error) {
                    Log.d("MyYandex", "Ошибка геокодирования: ${p0.toString()}")
                    if (continuation.isActive) continuation.resumeWith(Result.success(null))
                }
            }
        )
        continuation.invokeOnCancellation {
            searchSession?.cancel()
        }
        Log.d("MyYandex", "$searchSession")
    }

}