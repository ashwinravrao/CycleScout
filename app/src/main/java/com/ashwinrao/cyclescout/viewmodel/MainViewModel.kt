package com.ashwinrao.cyclescout.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.data.repository.RepositoryImpl
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel(private val repo: RepositoryImpl) : ViewModel() {
    private val tag = this.javaClass.simpleName
    var nextPageToken: String? = null
    var endOfResults = MutableLiveData(false)

    /**
     * Asynchronously fetches bicycle shops near the starting location. Page tokens are handled
     * by the above member variable `nextPageToken`. If the next_page_token response element is null
     * then we have reached the end of available results. At that point, we need to notify the activity
     * owner so that it can hide the loading animation, as no more requests will be processed by this method.
     */
    suspend fun fetchNearbyShops(locationBias: LatLng, refresh: Boolean): NearbySearch? {
        var result: NearbySearch? = null
        if (refresh) {
            endOfResults.value = false
            nextPageToken = null
        }
        if (endOfResults.value != true) {
            result = repo.fetchNearbyShops(locationBias, nextPageToken)
            nextPageToken = result?.nextPageToken
            if (nextPageToken.isNullOrBlank()) {
                withContext(Dispatchers.Main) {
                    endOfResults.value = true
                }
            }
            if (endOfResults.value!!) {
                Log.d(tag, "fetchNearbyShops: End of results. Next page token: $nextPageToken")
            }
        }
        return result
    }
}