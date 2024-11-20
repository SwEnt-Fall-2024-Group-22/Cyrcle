package com.github.se.cyrcle.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CustomViewModelFactory<T : ViewModel>(private val creator: () -> T) :
    ViewModelProvider.Factory {

  // Factory to unify the different factory of view Models
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    @Suppress("UNCHECKED_CAST") return creator() as T
  }
}
