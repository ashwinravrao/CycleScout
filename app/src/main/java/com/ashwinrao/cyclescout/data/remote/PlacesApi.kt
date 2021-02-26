package com.ashwinrao.cyclescout.data.remote

import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.data.remote.response.ShopAddress
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApi {

    /**
     * Fetches the nearest bicycle shops to our specified coordinates, matching our place type and keyword.
     * The type and keyword seem redundant but during manual testing it was found to provide more exhaustive results than
     * either parameter alone. Passing a non-empty String for the `pagetoken` param will fetch the next 20 (or the number of
     * available) results. This value is unfortunately not (easily) configurable.
     */
    @GET("nearbysearch/json?&rankby=distance&type=bicycle_store&keyword=bicycle+repair+shop")
    suspend fun fetchNearbyShops(
        @Query("location") formattedLatLng: String,
        @Query("pagetoken") nextPageToken: String): NearbySearch?

    /**
     * Request the street address for a particular shop in our result set, referencing the place_id field from the nearby search response object.
     */
    @GET("details/json?&fields=address_component")
    suspend fun fetchShopAddress(@Query("place_id") placeId: String): ShopAddress?
}