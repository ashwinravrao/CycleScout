package com.ashwinrao.cyclescout.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import com.ashwinrao.cyclescout.METER_TO_MILE_CONVERSION_FACTOR
import com.ashwinrao.cyclescout.data.remote.response.ShopAddress
import com.ashwinrao.cyclescout.data.repository.RepositoryImpl
import java.util.*

class DetailViewModel(private val repo: RepositoryImpl) : ViewModel() {

    suspend fun fetchShopAddress(placeId: String): ShopAddress? =
        repo.fetchShopAddress(placeId)

    // Extracts a coherent address string from the response returned by the Place Details endpoint.
    fun buildAddressString(addressComponents: List<ShopAddress.Result.AddressComponent>): String {
        val sb = StringBuilder()
        for (component in addressComponents) {
            when {
                component.types.contains("street_number") -> sb.append("${component.shortName} ")
                component.types.contains("route") -> sb.append("${component.shortName}, ")
                component.types.contains("locality") -> sb.append("${component.shortName}, ")
                component.types.contains("administrative_area_level_1") -> sb.append(component.shortName)
            }
        }
        return sb.toString()
    }

    // Calculates the distance between a shop and the starting location in miles, rounded to the tenths place.
    fun calculateProximity(
        firstLat: Double,
        firstLng: Double,
        secondLat: Double,
        secondLng: Double
    ): String {
        val first = Location("")
        val second = Location("")
        first.latitude = firstLat
        first.longitude = firstLng
        second.latitude = secondLat
        second.longitude = secondLng

        return String.format(
            Locale.getDefault(),
            "%.1f",
            (first.distanceTo(second) / METER_TO_MILE_CONVERSION_FACTOR)
        )
    }

}