package com.github.se.cyrcle.model.user

/**
 * A type alias for the amount of coins a user has. Allow more flexibility in the future if we want
 * to change the type of the coins.
 */
typealias Coin = Int

/**
 * Coin rewards that the user can get with multiple actions.
 */
const val PARKING_CREATION_REWARD: Coin = 100
const val PARKING_REVIEW_REWARD: Coin = 10
const val ACCOUNT_CREATION_REWARD: Coin = 100

/**
 * A class that represent a [Coin] container (hence the name), which can be used to store and
 * manipulate the amount of coins a user has.
 */
class Wallet(private var coins: Coin = 0) {

  /**
   * Add amount of coins to the wallet.
   *
   * @param amount the amount of coins to add. Must be positive.
   * @throws IllegalArgumentException if the amount is negative.
   */
  fun creditCoins(amount: Coin) {
    if (amount < 0) throw IllegalArgumentException("Amount must be positive")
    coins += amount
  }

  /**
   * Remove amount of coins to the wallet. This function does not check if the wallet is solvable.
   *
   * @param amount the amount of coins to remove. Must be positive.
   * @throws IllegalArgumentException if the amount is negative.
   */
  fun debitCoins(amount: Coin) {
    if (amount < 0) throw IllegalArgumentException("Amount must be positive")
    coins -= amount
  }

  /**
   * Return whether the user is solvable. A user is solvable if, after debiting the amount, the
   * amount of coins in the wallet is still superior to the credit threshold. This function does not
   * modify the value of the coins in the wallet, it's a simple projection.
   */
  fun isSolvable(amount: Coin, creditThreshold: Coin): Boolean {
    if (amount < 0) throw IllegalArgumentException("Amount must be positive")
    return (coins - amount) > creditThreshold
  }

  /**
   * Return the amount of coins currently in the mallet. WARNING: This method does not modify the
   * value of the coins in the wallet.
   */
  fun getCoins(): Coin {
    return coins
  }

  /** Creates an empty [Wallet]. It contains no coins. */
  companion object {
    fun empty(): Wallet {
      return Wallet()
    }
  }
}
