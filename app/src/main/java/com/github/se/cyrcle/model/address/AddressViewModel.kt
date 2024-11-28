package com.github.se.cyrcle.model.address

import android.util.Log
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * View model for an address.
 *
 * @property repository The repository for searching addresses.
 */
class AddressViewModel(private val repository: AddressRepository) : ViewModel() {

  private val _address = MutableStateFlow(Address())
  val address: StateFlow<Address>
    get() = _address

  private val _addressList = MutableStateFlow(listOf<Address>())
  val addressList: StateFlow<List<Address>>
    get() = _addressList

  /**
   * Searches for an address and updates the address state.
   *
   * @param point The point to search for.
   */
  fun search(point: Point) {
    val query = "${point.latitude()},${point.longitude()}"
    repository.search(
        query,
        onSuccess = { _address.value = it.first() },
        onFailure = { Log.e("AddressViewModel", "Failed to search for address", it) })
  }

  /**
   * Searches for an address and updates the address_list state.
   *
   * @param query The query to search for.
   */
  fun search(query: String) {
    repository.search(
        query,
        onSuccess = { _addressList.value = it },
        onFailure = { Log.e("AddressViewModel", "Failed to search for address", it) })
  }
}
