package com.github.se.cyrcle.model.address

import com.google.gson.Gson
import java.io.IOException
import javax.inject.Inject
import okhttp3.*
import org.json.JSONArray

class AddressRepositoryNominatim @Inject constructor(private val client: OkHttpClient) :
    AddressRepository {
  override fun search(
      query: String,
      onSuccess: (List<Address>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val url =
        HttpUrl.Builder()
            .scheme("https")
            .host("nominatim.openstreetmap.org")
            .addPathSegment("search")
            .addQueryParameter("q", query)
            .addQueryParameter("format", "json")
            .addQueryParameter("addressdetails", "1")
            .addQueryParameter("accept-language", "en")
            .build()
    val request = Request.Builder().url(url).header("User-Agent", "Cyrcle").build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                onFailure(e)
              }

              override fun onResponse(call: Call, response: Response) {
                response.use {
                  if (!response.isSuccessful) {
                    onFailure(Exception("Unexpected code $response"))
                    return
                  }

                  val body = response.body?.string()
                  if (body != null) {
                    onSuccess(addressFromBody(body))
                  } else {
                    onSuccess(emptyList())
                  }
                }
              }
            })
  }
  /**
   * Extracts an address list from the response body.
   *
   * @param body The response body.
   * @return The list of addresses.
   */
  private fun addressFromBody(body: String): List<Address> {

    val jsonArray = JSONArray(body)
    if (jsonArray.length() == 0) {
      return emptyList()
    }
    val addressList = mutableListOf<Address>()
    for (i in 0 until jsonArray.length()) {
      if (jsonArray.getJSONObject(i) != null) {
        val jsonObject = jsonArray.getJSONObject(i)
        val address = jsonObject.getJSONObject("address")
        val deserializedAddress = Gson().fromJson(address.toString(), Address::class.java)
        deserializedAddress.latitude = jsonObject.getString("lat")
        deserializedAddress.longitude = jsonObject.getString("lon")
        addressList.add(deserializedAddress)
      } else {
        break
      }
    }
    return addressList
  }
}
