package com.ashwinrao.cyclescout.data.repository

import com.ashwinrao.cyclescout.data.remote.PlacesApi
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.data.remote.response.ShopAddress
import com.google.android.gms.maps.model.LatLng
import org.koin.dsl.module

val repoModule = module {
    single { RepositoryImpl(get()) }
}

interface Repository {
    suspend fun fetchNearbyShops(locationBias: LatLng, nextPageToken: String? = null): NearbySearch?
    suspend fun fetchShopAddress(placeId: String): ShopAddress?
}

class RepositoryImpl(private val placesApi: PlacesApi) : Repository {
    override suspend fun fetchNearbyShops(locationBias: LatLng, nextPageToken: String?): NearbySearch? =
        placesApi.fetchNearbyShops("${locationBias.latitude},${locationBias.longitude}", nextPageToken ?: "")

    override suspend fun fetchShopAddress(placeId: String): ShopAddress? = placesApi.fetchShopAddress(placeId)
}