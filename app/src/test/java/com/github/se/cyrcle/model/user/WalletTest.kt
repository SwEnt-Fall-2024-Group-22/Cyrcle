package com.github.se.cyrcle.model.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WalletTest {

  @Test
  fun creditCoins_increasesCoinCount() {
    val wallet = Wallet()
    wallet.creditCoins(10)
    assertEquals(10, wallet.getCoins())
  }

  @Test
  fun debitCoins_decreasesCoinCount() {
    val wallet = Wallet(20)
    wallet.debitCoins(10)
    assertEquals(10, wallet.getCoins())
  }

  @Test
  fun creditCoins_throwsExceptionForNegativeAmount() {
    val wallet = Wallet()
    assertThrows(IllegalArgumentException::class.java) { wallet.creditCoins(-5) }
  }

  @Test
  fun debitCoins_throwsExceptionForNegativeAmount() {
    val wallet = Wallet()
    assertThrows(IllegalArgumentException::class.java) { wallet.debitCoins(-5) }
  }

  @Test
  fun empty_returnsWalletWithZeroCoins() {
    val wallet = Wallet.empty()
    assertEquals(0, wallet.getCoins())
  }
}
