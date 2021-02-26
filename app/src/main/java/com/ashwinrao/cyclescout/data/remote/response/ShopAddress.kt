package com.ashwinrao.cyclescout.data.remote.response


import com.google.gson.annotations.SerializedName

data class ShopAddress(
    @SerializedName("html_attributions")
    val htmlAttributions: List<Any>,
    @SerializedName("result")
    val result: Result,
    @SerializedName("status")
    val status: String // OK
) {
    data class Result(
        @SerializedName("address_components")
        val addressComponents: List<AddressComponent>
    ) {
        data class AddressComponent(
            @SerializedName("long_name")
            val longName: String, // 48
            @SerializedName("short_name")
            val shortName: String, // 48
            @SerializedName("types")
            val types: List<String>
        )
    }
}