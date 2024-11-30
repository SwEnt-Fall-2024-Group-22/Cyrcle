package com.github.se.cyrcle.model.address

import android.util.Log
import com.google.gson.annotations.SerializedName

const val maxDisplayNameLength = 32

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
    @SerializedName("continent") val continent: String = "",
    @SerializedName("lat") var latitude: String = "0.0",
    @SerializedName("lon") var longitude: String = "0.0"
) {
  /**
   * Among the fields of the address, we choose the most relevant ones to display. The order of the
   * fields is important. We choose not to display some fields at all, even if they are present.
   *
   * @return The most relevant fields of the address.
   */
  fun displayRelevantFields(): String {
    val fieldPriorities =
        listOf(publicName, road, house, cityBlock, neighbourhood, hamlet, suburb, city)
    return shortenString(
        fieldPriorities.filter { it.isNotEmpty() }.take(3).joinToString(", "), maxDisplayNameLength)
  }

  /**
   * If the string is longer than length. Shorten the string by removing everything after the last
   * comma
   *
   * @param string the string to shorten
   * @param length the maximum length of the string
   */
  private fun shortenString(string: String, length: Int): String {
    Log.d("Address", "Shortening string $string to length $length")
    if (string.length <= length) {
      return string
    }
    val lastComma = string.lastIndexOf(",")
    if (lastComma == -1) {
      return string
    }
    return shortenString(string.substring(0, lastComma), length)
  }

  /** Function that format the Address to display its suggestion name. */
  fun suggestionFormatDisplayName(): String {
    val fieldPriorities = listOf(publicName, road, city, country)
    return shortenString(
        fieldPriorities.filter { it.isNotEmpty() }.joinToString(", "), maxDisplayNameLength)
  }
}
