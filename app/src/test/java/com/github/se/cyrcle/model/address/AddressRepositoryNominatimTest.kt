package com.github.se.cyrcle.model.address

import com.mapbox.geojson.Point
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddressRepositoryNominatimTest {
  @Mock private lateinit var mockClient: OkHttpClient
  @Mock private lateinit var mockCall: Call

  private lateinit var addressRepositoryNominatim: AddressRepositoryNominatim
  private val whiteHouse = Point.fromLngLat(-77.036571, 38.897870)
  private val whiteHouseString = "${whiteHouse.latitude()},${whiteHouse.longitude()}"

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    addressRepositoryNominatim = AddressRepositoryNominatim(mockClient)
    `when`(mockClient.newCall(any())).thenReturn(mockCall)
  }

  @Test
  fun testSearchMakesCallsOnClientAndCall() {
    addressRepositoryNominatim.search(whiteHouseString, {}, {})
    verify(mockClient).newCall(any())
    verify(mockCall).enqueue(any())
  }

  @Test
  fun testSearchSuccessWithValidResponse() {
    val mockResponse =
        MockResponse(
            200,
            """[      
          {
            "place_id": 303453880,
            "licence": "Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright",
            "osm_type": "way",
            "osm_id": 238241022,
            "lat": "38.897699700000004",
            "lon": "-77.03655315",
            "class": "office",
            "type": "government",
            "place_rank": 30,
            "importance": 0.63472115416811,
            "addresstype": "office",
            "name": "White House",
            "display_name": "White House, 1600, Pennsylvania Avenue Northwest, Ward 2, Washington, District of Columbia, 20500, United States",
            "address": {
              "office": "White House",
              "house_number": "1600",
              "road": "Pennsylvania Avenue Northwest",
              "borough": "Ward 2",
              "city": "Washington",
              "state": "District of Columbia",
              "ISO3166-2-lvl4": "US-DC",
              "postcode": "20500",
              "country": "United States",
              "country_code": "us"
            },
            "boundingbox": [
              "38.8974908",
              "38.8979110",
              "-77.0368537",
              "-77.0362519"
            ]
          }
        ]"""
                .trimIndent())

    setupMockResponse(mockResponse)

    var resultAddress: Address? = null
    addressRepositoryNominatim.search(whiteHouseString, { address -> resultAddress = address }, {})

    assertNotNull(resultAddress)

    assertEquals("Washington", resultAddress?.city)
    assertEquals("United States", resultAddress?.country)
    assertEquals("White House", resultAddress?.publicName)
    assertEquals("1600", resultAddress?.house)
    assertEquals("Pennsylvania Avenue Northwest", resultAddress?.road)
    assertEquals("20500", resultAddress?.postcode)
    assertEquals("District of Columbia", resultAddress?.region)
  }

  @Test
  fun testSearchFailureWithNetworkError() {
    `when`(mockCall.enqueue(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as Callback
      callback.onFailure(mockCall, IOException("Network error"))
    }

    var resultException: Exception? = null
    addressRepositoryNominatim.search(
        whiteHouseString, {}, { exception -> resultException = exception })

    assertNotNull(resultException)
    assertEquals("Network error", resultException?.message)
  }

  @Test
  fun testSearchFailureWithUnsuccessfulResponse() {
    val mockResponse = MockResponse(404, "Not Found")
    setupMockResponse(mockResponse)

    var resultException: Exception? = null
    addressRepositoryNominatim.search(
        whiteHouseString, {}, { exception -> resultException = exception })

    assertNotNull(resultException)
    assertEquals(
        "Unexpected code Response{protocol=http/1.1, code=404, message=Error, url=http://localhost/}",
        resultException?.message)
  }

  @Test
  fun testSearchSuccessWithEmptyResponse() {
    val mockResponse = MockResponse(200, "[]")
    setupMockResponse(mockResponse)

    var resultAddress: Address? = null
    addressRepositoryNominatim.search("0.0,0.0", { address -> resultAddress = address }, {})

    assertNotNull(resultAddress)
    assertEquals(Address(), resultAddress)
  }

  @Test
  fun testSearchRequestUrl() {
    val urlCaptor = argumentCaptor<Request>()
    `when`(mockClient.newCall(urlCaptor.capture())).thenReturn(mockCall)

    setupMockResponse(MockResponse(200, "[]"))

    addressRepositoryNominatim.search(whiteHouseString, {}, {})

    val capturedRequest = urlCaptor.firstValue
    assertEquals(
        "https://nominatim.openstreetmap.org/search?q=38.89787%2C-77.036571&format=json&addressdetails=1",
        capturedRequest.url.toString())
    assertEquals("Cyrcle", capturedRequest.header("User-Agent"))
  }

  private fun setupMockResponse(mockResponse: MockResponse) {
    `when`(mockCall.enqueue(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as Callback
      callback.onResponse(mockCall, mockResponse.toResponse())
    }
  }

  private data class MockResponse(val code: Int, val body: String) {
    fun toResponse(): Response {
      return Response.Builder()
          .code(code)
          .message(if (code == 200) "OK" else "Error")
          .protocol(Protocol.HTTP_1_1)
          .request(Request.Builder().url("http://localhost/").build())
          .body(body.toResponseBody(null))
          .build()
    }
  }
}
