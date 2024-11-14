package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.address.Address
import com.github.se.cyrcle.model.address.AddressRepository
import javax.inject.Inject

class MockAddressRepository @Inject constructor() : AddressRepository {
  private val mockAddress =
      Address(
          publicName = "Mock Park",
          house = "Mock House",
          road = "Mock Road",
          suburb = "Mock Suburb",
          city = "Mock City",
          region = "Mock State",
          postcode = "Mock Postcode",
          country = "Mock Country")

  override fun search(query: String, onSuccess: (Address) -> Unit, onFailure: (Exception) -> Unit) {
    if (query.isEmpty() || query.isBlank()) {
      onFailure(Exception("Query is empty"))
      return
    }
    onSuccess(mockAddress)
  }
}
