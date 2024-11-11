package com.github.se.cyrcle.model.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UserTest {

  @Test
  fun testEqualsAndHashCode() {
    val user1 = TestInstancesUser.user1
    val copyUser1 = TestInstancesUser.user1
    val newUser = TestInstancesUser.newUser
    TestInstancesUser.updatedUser
    assertEquals(user1, copyUser1)
    assertNotEquals(user1, newUser)
    assertEquals(user1.hashCode(), copyUser1.hashCode())
    assertNotEquals(user1.hashCode(), newUser.hashCode())
  }
}
