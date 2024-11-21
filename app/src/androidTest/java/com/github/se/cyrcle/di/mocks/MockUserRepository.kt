package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserRepository
import javax.inject.Inject

class MockUserRepository @Inject constructor() : UserRepository {
  private var uid = 0
  private val _users = mutableListOf<User>()
  val users: List<User> = _users

  override fun onSignIn(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun getUid(): String {
    return (uid++).toString()
  }

  override fun getUserById(
      userId: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (userId == "" || _users.none { it.public.userId == userId })
        onFailure(Exception("Error getting user"))
    else onSuccess(_users.find { it.public.userId == userId }!!)
  }

  override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    if (_users.none { it.public.userId == "" }) onSuccess(_users)
    else onFailure(Exception("Error getting users"))
  }

  override fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (user.public.userId == "") onFailure(Exception("Error adding user"))
    else {
      _users.add(user)
      onSuccess()
    }
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (user.public.userId == "" || _users.none { it.public.userId == user.public.userId })
        onFailure(Exception("Error updating user"))
    else {
      _users.remove(_users.find { it.public.userId == user.public.userId })
      _users.add(user)
      onSuccess()
    }
  }

  override fun deleteUserById(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (userId == "" || _users.none { it.public.userId == userId })
        onFailure(Exception("Error deleting user"))
    else {
      _users.remove(_users.find { it.public.userId == userId })
      onSuccess()
    }
  }
}
