package com.github.se.cyrcle.model.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UserTest {
  @Test
  fun testConstructor() {
    val user = UserTestInstances.user1
    assertEquals("user1", user.userId)
    assertEquals("john_doe", user.username)
    assertEquals("John", user.firstName)
    assertEquals("Doe", user.lastName)
    assertEquals("john.doe@example.com", user.email)
    assertEquals("https://example.com/profile/john_doe.jpg", user.profilePictureUrl)
    assertEquals(listOf("Test_spot_1", "Test_spot_2"), user.favoriteParkings)
  }

  @Test
  fun testEqualsAndHashCode() {
    val user1 = UserTestInstances.user1
    val copyUser1 = UserTestInstances.user1
    val newUser = UserTestInstances.newUser
    UserTestInstances.updatedUser
    assertEquals(user1, copyUser1)
    assertNotEquals(user1, newUser)
    assertEquals(user1.hashCode(), copyUser1.hashCode())
    assertNotEquals(user1.hashCode(), newUser.hashCode())
  }
}
