package com.ashwinrao.cyclescout.data.repository

import com.ashwinrao.cyclescout.data.remote.PlacesApi
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.google.android.gms.maps.model.LatLng
import org.koin.dsl.module

val repoModule = module {
    single { RepositoryImpl(get()) }
}

interface Repository {
    suspend fun fetchNearbyShops(locationBias: LatLng, nextPageToken: String? = null): NearbySearch?
}

class RepositoryImpl(private val placesApi: PlacesApi) : Repository {
    override suspend fun fetchNearbyShops(locationBias: LatLng, nextPageToken: String?): NearbySearch? =
        placesApi.fetchNearbyShops("${locationBias.latitude},${locationBias.longitude}", nextPageToken ?: "")
}