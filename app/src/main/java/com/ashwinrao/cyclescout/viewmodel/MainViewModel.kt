package com.ashwinrao.cyclescout.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.data.repository.RepositoryImpl
import com.google.android.gms.maps.model.LatLng

class MainViewModel(private val repo: RepositoryImpl) : ViewModel() {
    var endOfResults = false
    var nextPageToken: String? = null

    var endOfResultsLiveData = MutableLiveData(endOfResults)

    suspend fun fetchNearbyShops(locationBias: LatLng, refresh: Boolean): NearbySearch? {
        var result: NearbySearch? = null
        if (refresh) {
            endOfResults = false
            nextPageToken = null
        }
        if (!endOfResults) {
            result = repo.fetchNearbyShops(locationBias, nextPageToken)
            nextPageToken = result?.nextPageToken
            if (nextPageToken.isNullOrBlank()) endOfResults = true
        }
        return result
    }
}