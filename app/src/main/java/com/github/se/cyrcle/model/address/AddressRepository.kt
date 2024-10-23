package com.github.se.cyrcle.model.address

/** Repository for searching addresses. */
interface AddressRepository {
  /**
   * Search for an address.
   *
   * @param query The query to search for.
   * @param onSuccess The callback to be called when the search is successful.
   * @param onFailure The callback to be called when the search fails.
   */
  fun search(query: String, onSuccess: (Address) -> Unit, onFailure: (Exception) -> Unit)
}
