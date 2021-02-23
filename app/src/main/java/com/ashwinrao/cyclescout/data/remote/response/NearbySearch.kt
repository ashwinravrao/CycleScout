package com.ashwinrao.cyclescout.data.remote.response


import com.google.gson.annotations.SerializedName

data class NearbySearch(
    @SerializedName("html_attributions")
    val htmlAttributions: List<Any>,
    @SerializedName("next_page_token")
    val nextPageToken: String, // ATtYBwJFL7oIFomWNpXRC1PKslpTdkneeq_LFOGZD2nCI-Af3brt9i2SD6_UlhmEBPS3TowqtVNP8wVX3ScU7xHE9oCYRHt4NZ2MdHzVGGlnaS-PcVdgGTbZkDHu86B2ByVc2CyuQcKX7QRnHBCL-QYOqunW_GsVaF1N4W9iY_NGfDILMijPmvtDnZ-6OkBtUs5rU5KqOyoibBtCcKvW5S43K4qLJX6XtjHrxI1XXUXLgtOY0b1jIoIQ9DfL0vq9AZHcmAwLJN9xS7zNtV8EnMt3QBLJWLOmZdBsyridJm-pVPEPHD37ekO-mz_L1_eHS5QEfJrSQkiQ2OrnZ9Kp1usyvPnqhN8gQisA76KkiDI9KFa7M__QR4MzofkkynUeXpOj5eOUQklzvY4Wf_avTkf_bu9i-v0_U6xiniQo6P4JBM14-s4Z7RHQvaATVSI8cKcRte3dqQs
    @SerializedName("results")
    val results: List<Result>,
    @SerializedName("status")
    val status: String // OK
) {
    data class Result(
        @SerializedName("business_status")
        val businessStatus: String, // OPERATIONAL
        @SerializedName("geometry")
        val geometry: Geometry,
        @SerializedName("icon")
        val icon: String, // https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/bar-71.png
        @SerializedName("name")
        val name: String, // Cruise Bar
        @SerializedName("opening_hours")
        val openingHours: OpeningHours,
        @SerializedName("permanently_closed")
        val permanentlyClosed: Boolean, // true
        @SerializedName("photos")
        val photos: List<Photo>,
        @SerializedName("place_id")
        val placeId: String, // ChIJi6C1MxquEmsR9-c-3O48ykI
        @SerializedName("plus_code")
        val plusCode: PlusCode,
        @SerializedName("price_level")
        val priceLevel: Int, // 2
        @SerializedName("rating")
        val rating: Double, // 4.8
        @SerializedName("reference")
        val reference: String, // ChIJi6C1MxquEmsR9-c-3O48ykI
        @SerializedName("scope")
        val scope: String, // GOOGLE
        @SerializedName("types")
        val types: List<String>,
        @SerializedName("user_ratings_total")
        val userRatingsTotal: Int, // 1105
        @SerializedName("vicinity")
        val vicinity: String // Level 1, 2 and 3, Overseas Passenger Terminal, Circular Quay W, The Rocks
    ) {
        data class Geometry(
            @SerializedName("location")
            val location: Location,
            @SerializedName("viewport")
            val viewport: Viewport
        ) {
            data class Location(
                @SerializedName("lat")
                val lat: Double, // -33.8587323
                @SerializedName("lng")
                val lng: Double // 151.2100055
            )

            data class Viewport(
                @SerializedName("northeast")
                val northeast: Northeast,
                @SerializedName("southwest")
                val southwest: Southwest
            ) {
                data class Northeast(
                    @SerializedName("lat")
                    val lat: Double, // -33.85739417010727
                    @SerializedName("lng")
                    val lng: Double // 151.2112278798927
                )

                data class Southwest(
                    @SerializedName("lat")
                    val lat: Double, // -33.86009382989272
                    @SerializedName("lng")
                    val lng: Double // 151.2085282201073
                )
            }
        }

        data class OpeningHours(
            @SerializedName("open_now")
            val openNow: Boolean // false
        )

        data class Photo(
            @SerializedName("height")
            val height: Int, // 575
            @SerializedName("html_attributions")
            val htmlAttributions: List<String>,
            @SerializedName("photo_reference")
            val photoReference: String, // ATtYBwIv7JuAxvjZA6Jr3aDABxV3kDNzW0eFAyhSxj_WmW8Z0SdbVGmGRR_KGi6cfKgai-oUSQ2fp6d5qkKjc2M-LKvY7fuR2czdwHVdY7gtS8iEqyhMieznSTNkElpJv5A6Uqd3Vy6Sfd9LFYP1LeK9w7BmzX2oa0xcbUDBphTyevlgUAq4
            @SerializedName("width")
            val width: Int // 766
        )

        data class PlusCode(
            @SerializedName("compound_code")
            val compoundCode: String, // 46R6+G2 The Rocks, New South Wales
            @SerializedName("global_code")
            val globalCode: String // 4RRH46R6+G2
        )
    }
}