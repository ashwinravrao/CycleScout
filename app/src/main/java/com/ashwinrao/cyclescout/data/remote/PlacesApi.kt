package com.ashwinrao.cyclescout.data.remote

import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
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
     * Request a photo for each shop in our result set. In order to get the correct photo, we must pass in the reference String from
     * NearbySearch.Result.Photo
     *
     * @return URL for an image in its original aspect ratio with a max width of 1000px (later resized for thumbnails)
     */
    @GET("photo?&maxwidth=1000")
    suspend fun fetchPhotoURL(@Query("photoreference") photoReference: String): String
}