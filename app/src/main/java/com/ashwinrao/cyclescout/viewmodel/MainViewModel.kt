package com.ashwinrao.cyclescout.viewmodel

import androidx.lifecycle.ViewModel
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.data.repository.RepositoryImpl
import com.google.android.gms.maps.model.LatLng

class MainViewModel(private val repo: RepositoryImpl) : ViewModel() {
    var nextPageToken: String? = null

    suspend fun fetchNearbyShops(locationBias: LatLng): NearbySearch? {
        return repo.fetchNearbyShops(locationBias)
    }
}