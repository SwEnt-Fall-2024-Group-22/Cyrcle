package com.github.se.cyrcle.model.address

import android.util.Log
import com.google.gson.annotations.SerializedName

/**
 * Represents an address.
 *
 * @property publicName The public name of the address. This can be for example the name of a
 *   hospital or a park.
 * @property house The house number or name.
 * @property road The road name.
 * @property cityBlock The city block, residential, farm, farmyard, industrial, commercial or
 *   retail.
 * @property neighbourhood The neighbourhood, quarter or allotments.
 * @property hamlet The hamlet, isolated dwelling or croft.
 * @property suburb The suburb, city district, district, borough or subdivision.
 * @property city The city, town, municipality or village.
 * @property postcode The postcode.
 * @property region The region, state, state district, county or ISO3166-2-lvl.
 * @property country The country.
 * @property continent The continent.
 */
data class Address(
    @SerializedName(
        "amenity",
        alternate =
            [
                "emergency",
                "historic",
                "military",
                "natural",
                "landuse",
                "place",
                "railway",
                "man_made",
                "aerialway",
                "boundary",
                "aeroway",
                "club",
                "craft",
                "leisure",
                "office",
                "mountain_pass",
                "shop",
                "tourism",
                "bridge",
                "tunnel",
                "waterway"])
    val publicName: String = "",
    @SerializedName("house_number", alternate = ["house_name"]) val house: String = "",
    @SerializedName("road") val road: String = "",
    @SerializedName(
        "city_block",
        alternate = ["residential", "farm", "farmyard", "industrial", "commercial", "retail"])
    val cityBlock: String = "",
    @SerializedName("neighbourhood", alternate = ["quarter", "allotments"])
    val neighbourhood: String = "",
    @SerializedName("hamlet", alternate = ["isolated_dwelling, croft"]) val hamlet: String = "",
    @SerializedName("suburb", alternate = ["city_district", "district", "borough", "subdivision"])
    val suburb: String = "",
    @SerializedName("city", alternate = ["municipality", "village, town"]) val city: String = "",
    @SerializedName("postcode") val postcode: String = "",
    @SerializedName("region", alternate = ["state", "state_district", "county", "ISO3166-2-lvl"])
    val region: String = "",
    @SerializedName("country") val country: String = "",
    @SerializedName("continent") val continent: String = ""
) {
  /**
   * Among the fields of the address, we choose the most relevant ones to display.
   *
   * @return The most relevant fields of the address.
   */
  fun displayRelevantFields(): String {
    // The order of the fields is important. We choose not to display some fields at all, even if
    // they are present.
    val fieldPriorities =
        listOf(publicName, house, road, cityBlock, neighbourhood, hamlet, suburb, city)

    // We filter out empty fields and take the first three non-empty fields
    val nonemptyFields = fieldPriorities.filter { it.isNotEmpty() }
    Log.d("Address", "nonemptyFields: $nonemptyFields")
    val relevantFields = nonemptyFields.take(3)
    Log.d("Address", "relevantFields: $relevantFields")
    return fieldPriorities.filter { it.isNotEmpty() }.take(3).joinToString(", ")
  }
}
