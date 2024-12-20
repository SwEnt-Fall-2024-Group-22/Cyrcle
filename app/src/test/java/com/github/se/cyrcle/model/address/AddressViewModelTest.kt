package com.github.se.cyrcle.model.address

import com.mapbox.geojson.Point
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class AddressViewModelTest {
  @Mock private lateinit var addressRepositoryNominatim: AddressRepositoryNominatim
  private lateinit var addressViewModel: AddressViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    addressViewModel = AddressViewModel(addressRepositoryNominatim)
  }

  @Test
  fun testSearchCallsRepository() {
    val point = Point.fromLngLat(0.0, 0.0)
    addressViewModel.search(point)

    // Check if the search method was called with the correct query
    verify(addressRepositoryNominatim).search(eq("0.0,0.0"), any(), any())
  }

  @Test
  fun testSearchSetsAddress() {
    `when`(addressRepositoryNominatim.search(any(), any(), any())).then {
      it.getArgument<(List<Address>) -> Unit>(1)(
          listOf(
              Address(
                  city = "Paris",
                  country = "France",
              )))
    }
    val point = Point.fromLngLat(0.0, 0.0)
    addressViewModel.search(point)

    // Check that the address state is not empty
    assert(addressViewModel.address.value != Address())
  }

  @Test
  fun testSearchSetsAddressList() {
    `when`(addressRepositoryNominatim.search(any(), any(), any())).then {
      it.getArgument<(List<Address>) -> Unit>(1)(
          listOf(
              Address(
                  city = "Paris",
                  country = "France",
              )))
    }
    addressViewModel.search("Paris")

    assert(
        addressViewModel.addressList.value.first() ==
            Address(
                city = "Paris",
                country = "France",
            ))
  }
}
