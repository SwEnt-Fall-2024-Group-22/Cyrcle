package com.github.se.cyrcle.model.address

import org.junit.Assert.assertEquals
import org.junit.Test

class AddressTest {

  @Test
  fun testConstructor() {
    val address =
        Address(
            country = "Switzerland", city = "Lausanne", road = "Avenue de la Gare", house = "14")
    assertEquals("Switzerland", address.country)
    assertEquals("Lausanne", address.city)
    assertEquals("Avenue de la Gare", address.road)
    assertEquals("14", address.house)

    // Assert other fields are empty
    assertEquals("", address.publicName)
    assertEquals("", address.cityBlock)
    assertEquals("", address.neighbourhood)
    assertEquals("", address.hamlet)
    assertEquals("", address.suburb)
    assertEquals("", address.postcode)
    assertEquals("", address.region)
    assertEquals("", address.continent)
  }

  @Test
  fun testConstructorWithEmptyValues() {
    val address = Address()
    assertEquals("", address.country)
    assertEquals("", address.city)
    assertEquals("", address.road)
    assertEquals("", address.house)
  }

  @Test
  fun testDisplayRelevantFields() {
    val address1 =
        Address(
            country = "Switzerland", city = "Lausanne", road = "Avenue de la Gare", house = "14")
    assertEquals("14, Avenue de la Gare, Lausanne", address1.displayRelevantFields())

    val address2 =
        Address(
            publicName = "Palais de L'Élysée",
            city = "Paris",
            road = "Rue du Faubourg Saint-Honoré",
            house = "55")
    assertEquals(
        "Palais de L'Élysée, 55, Rue du Faubourg Saint-Honoré", address2.displayRelevantFields())
  }
}