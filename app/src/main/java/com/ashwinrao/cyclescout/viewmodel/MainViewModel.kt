package com.ashwinrao.cyclescout.viewmodel

import androidx.lifecycle.ViewModel
import com.ashwinrao.cyclescout.data.repository.RepositoryImpl
import com.google.android.gms.maps.model.LatLng

class MainViewModel(private val repo: RepositoryImpl) : ViewModel() {
    suspend fun fetchNearbyShops(locationBias: LatLng) = repo.fetchNearbyShops(locationBias)
}