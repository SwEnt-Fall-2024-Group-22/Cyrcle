package com.github.se.cyrcle.model.address

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient

/**
 * View model for an address.
 *
 * @property repository The repository for searching addresses.
 */
class AddressViewModel(private val repository: AddressRepository) : ViewModel() {
  private val _address = MutableStateFlow(Address())
  val address: StateFlow<Address>
    get() = _address

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddressViewModel(AddressRepositoryNominatim(OkHttpClient())) as T
          }
        }
  }

  /**
   * Searches for an address and updates the address state.
   *
   * @param point The point to search for.
   */
  fun search(point: Point) {
    val query = "${point.latitude()},${point.longitude()}"
    repository.search(
        query,
        onSuccess = { _address.value = it },
        onFailure = { Log.e("AddressViewModel", "Failed to search for address", it) })
  }
}
