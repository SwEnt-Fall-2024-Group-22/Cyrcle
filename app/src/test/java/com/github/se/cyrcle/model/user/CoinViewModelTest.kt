package com.github.se.cyrcle.model.user

import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CoinViewModelTest {

  private lateinit var userViewModel: UserViewModel
  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var parkingRepository: ParkingRepository
  @Mock private lateinit var imageRepository: ImageRepository
  @Mock private lateinit var authenticator: AuthenticationRepository

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    userViewModel = UserViewModel(userRepository, parkingRepository, imageRepository, authenticator)
  }

  @Test
  fun creditCoins_increasesCoinCount() {
    val viewModel = CoinViewModel(userViewModel)
    viewModel.creditCoins(10)
    assertEquals(10, viewModel.getCoins())
  }

  @Test
  fun debitCoins_decreasesCoinCount() {
    val viewModel = CoinViewModel(userViewModel)
    viewModel.creditCoins(20)
    viewModel.debitCoins(10)
    assertEquals(10, viewModel.getCoins())
  }

  @Test
  fun creditCoins_throwsExceptionForNegativeAmount() {
    val viewModel = CoinViewModel(userViewModel)
    assertThrows(IllegalArgumentException::class.java) { viewModel.creditCoins(-5) }
  }

  @Test
  fun debitCoins_throwsExceptionForNegativeAmount() {
    val viewModel = CoinViewModel(userViewModel)
    assertThrows(IllegalArgumentException::class.java) { viewModel.debitCoins(-5) }
  }

  @Test
  fun debitCoins_throwsExceptionForInsufficientFunds() {
    val viewModel = CoinViewModel(userViewModel)
    viewModel.creditCoins(5)
    assertFalse(viewModel.debitCoins(10))
    assertEquals(5, viewModel.getCoins())
  }

  @Test
  fun getCoins_returnsCorrectCoinCount() {
    val viewModel = CoinViewModel(userViewModel)
    viewModel.creditCoins(15)
    assertEquals(15, viewModel.getCoins())
  }
}
