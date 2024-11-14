package com.github.se.cyrcle.model.user

/**
 * A ViewModel for the coins of a user. If the user is signed in, the ViewModel will manage the user
 * currencies. If the user is not signed in, the ViewModel will manage the coins but the changes
 * won't be persistent
 *
 * @param userViewModel The ViewModel of the user to get info about the current state of the user
 */
class CoinViewModel(userViewModel: UserViewModel) {
  private val currentUser = userViewModel.currentUser

  private var anonymousWallet: Wallet = Wallet.empty()

  private val creditThreshold = 0

  /**
   * The amount of coins the user has.
   *
   * @param amount The amount of coins the user has. Must be positive.
   */
  fun creditCoins(amount: Coin) {
    if (amount < 0) throw IllegalArgumentException("Amount must be positive")
    currentUser.value?.details?.wallet?.creditCoins(amount) ?: anonymousWallet.creditCoins(amount)
  }

  /**
   * Remove amount of coins to the wallet.
   *
   * @param amount the amount of coins to remove. Must be positive.
   * @return true if the coins were debited, false otherwise (the user is not solvable)
   */
  fun debitCoins(amount: Coin): Boolean {
    if (amount < 0) throw IllegalArgumentException("Amount must be positive")

    val isSolvable =
        currentUser.value?.details?.wallet?.isSolvable(amount, creditThreshold)
            ?: anonymousWallet.isSolvable(amount, creditThreshold)
    if (isSolvable)
        currentUser.value?.details?.wallet?.debitCoins(amount) ?: anonymousWallet.debitCoins(amount)

    return isSolvable
  }

  /**
   * Return the amount of coins currently in the mallet. WARNING: This method does not modify the
   * value of the coins in the wallet.
   */
  fun getCoins(): Coin {
    return currentUser.value?.details?.wallet?.getCoins() ?: anonymousWallet.getCoins()
  }
}
