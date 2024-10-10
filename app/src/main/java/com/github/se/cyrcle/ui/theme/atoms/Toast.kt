package com.github.se.cyrcle.ui.theme.atoms

import android.content.Context
import android.widget.Toast

/** List the different possibility for a message via a `Toast`. */
enum class ToastSeverity {
  INFO,
  MESSAGE,
  WARNING,
  ALERT
}

class Toast {
  /** Create a `Toast` with a particular severity. */
  fun toast(context: Context, message: String, severity: ToastSeverity) {
    val duration =
        when (severity) {
          ToastSeverity.INFO -> Toast.LENGTH_SHORT
          ToastSeverity.MESSAGE -> Toast.LENGTH_SHORT
          ToastSeverity.WARNING -> Toast.LENGTH_SHORT
          ToastSeverity.ALERT -> Toast.LENGTH_SHORT
        }

    val messageL =
        when (severity) {
          ToastSeverity.INFO -> message
          ToastSeverity.MESSAGE -> message
          ToastSeverity.WARNING -> message.uppercase()
          ToastSeverity.ALERT -> "! " + message.uppercase() + " !"
        }

    Toast.makeText(context, messageL, duration).show()
  }
}
