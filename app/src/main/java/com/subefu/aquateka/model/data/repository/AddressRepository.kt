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


class AddressRepository {
    private val searchManager: SearchManager = SearchFactory.getInstance()
        .createSearchManager(SearchManagerType.ONLINE)

    private var searchSession: Session? = null

    fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
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

                    onResult(addressText)
                }

                override fun onSearchError(error: com.yandex.runtime.Error) {
                    onResult(null)
                }
            }
        )
    }
}
