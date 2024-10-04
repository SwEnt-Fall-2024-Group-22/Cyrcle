package com.github.se.cyrcle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.se.cyrcle.ui.authentication.SignInScreen
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth



  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
    auth.currentUser?.let {
      // Sign out the user if they are already signed in
      auth.signOut()
    }

    setContent {
      CyrcleTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          SignInScreen()
        }
      }
    }
  }
}
