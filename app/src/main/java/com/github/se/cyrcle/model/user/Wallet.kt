package com.github.se.cyrcle.model.user

typealias Coin = Int

/**
 * TODO
 */
class Wallet {

  private var coins: Coin = 0

  fun isSolvable(coinsToHave: Coin): Boolean {
    return coins >= coinsToHave
  }

  /**
   * TODO
   */
    companion object {
        fun empty(): Wallet {
            return Wallet()
        }
    }
}