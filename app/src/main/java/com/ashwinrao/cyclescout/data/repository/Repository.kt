package com.ashwinrao.cyclescout.data.repository

import com.ashwinrao.cyclescout.data.remote.PlacesApi
import com.ashwinrao.cyclescout.data.remote.Response
import com.google.android.gms.maps.model.LatLng
import org.koin.dsl.module

val repoModule = module {
    single { RepositoryImpl(get()) }
}

interface Repository {
    suspend fun fetchNearbyShops(locationBias: LatLng): Response?
}

class RepositoryImpl(private val placesApi: PlacesApi) : Repository {
    override suspend fun fetchNearbyShops(locationBias: LatLng): Response? = placesApi.fetchNearbyShops("${locationBias.latitude},${locationBias.longitude}")
}