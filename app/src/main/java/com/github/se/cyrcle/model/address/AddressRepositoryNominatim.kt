package com.github.se.cyrcle.model.address

import com.google.gson.Gson
import java.io.IOException
import javax.inject.Inject
import okhttp3.*
import org.json.JSONArray

class AddressRepositoryNominatim @Inject constructor(private val client: OkHttpClient) :
    AddressRepository {
  override fun search(query: String, onSuccess: (Address) -> Unit, onFailure: (Exception) -> Unit) {
    val url =
        HttpUrl.Builder()
            .scheme("https")
            .host("nominatim.openstreetmap.org")
            .addPathSegment("search")
            .addQueryParameter("q", query)
            .addQueryParameter("format", "json")
            .addQueryParameter("addressdetails", "1")
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
                    val address = addressFromBody(body)
                    onSuccess(address)
                  } else {
                    onSuccess(Address())
                  }
                }
              }
            })
  }

  /** Extracts an address from the response body. */
  private fun addressFromBody(body: String): Address {
    val jsonArray = JSONArray(body)
    if (jsonArray.length() == 0) {
      return Address()
    }
    val jsonObject = jsonArray.getJSONObject(0)
    val address = jsonObject.getJSONObject("address")
    return Gson().fromJson(address.toString(), Address::class.java)
  }
}
