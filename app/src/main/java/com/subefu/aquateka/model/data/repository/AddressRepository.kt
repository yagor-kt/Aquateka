package com.subefu.aquateka.model.data.repository

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

    private val searchManager: SearchManager =
        SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)

    suspend fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double
    ): String? = suspendCancellableCoroutine { cont ->

        val targetPoint = Point(latitude, longitude)
        val searchOptions = SearchOptions().apply {
            searchTypes = SearchType.GEO.value
        }

        val session = searchManager.submit(
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

                    if (cont.isActive) {
                        cont.resume(addressText, null)
                    }
                }

                override fun onSearchError(error: com.yandex.runtime.Error) {
                    if (cont.isActive) {
                        cont.resume(null, null)
                    }
                }
            }
        )

        cont.invokeOnCancellation {
            session.cancel()
        }
    }
}