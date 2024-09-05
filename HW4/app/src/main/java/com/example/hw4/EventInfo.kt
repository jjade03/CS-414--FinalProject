package com.example.hw4

import com.google.gson.annotations.SerializedName

data class EventInfo(
    val _embedded: EmbeddedData?,
)

data class EmbeddedData(
    val events: List<EventsInfo>
)

data class EventsInfo(
    val name: String, // Name of event
    val url: String, // Ticket url
    val images: List<ImageList>,
    val dates: Dates,
    @SerializedName("_embedded") val locationInfo: Location?,
    val priceRanges: List<PriceInfo>
)

data class Dates(
    val start: DateTime
)

data class DateTime(
    val localDate: String,  // Date of event
    val localTime: String?   // Time of event
)

data class Location(
    val venues: List<VenueInfo>?
)

data class VenueInfo(
    @SerializedName("name") val venueName: String,  // Event venue name
    val city: CityInfo,
    val state: StateInfo,
    val address: AddressInfo
)

data class CityInfo(
    @SerializedName("name") val cityName: String,   // City of event
)

data class StateInfo(
    @SerializedName("name") val stateName: String  // State of event
)

data class AddressInfo(
    @SerializedName("line1") val eventAddress: String   // Address of event
)

data class ImageList(
    @SerializedName("url") val imageUrl: String, // Image of event
    val width: Int,
    val height: Int
)

data class PriceInfo(
    @SerializedName("min") val minPrice: Double?,
    @SerializedName("max") val maxPrice: Double?
)
