package com.github.se.cyrcle.ui.theme.molecules

import android.media.Image
import androidx.compose.runtime.Composable

/**
 * List the different possibility for a carousel
 * <ul>
 * <li> {@code MANUAL} : Wait for the user input to change the image.</li>
 * <li> {@code AUTOMATED} : Will rotate on its own.</li>
 * <li> {@code AUTOMATED_THEN_MANUAL} : Will rotate until the first user input, then will stay in
 *   manual mode</li>
 * </ul>
 */
enum class CarouselMode {
  MANUAL,
  AUTOMATED,
  AUTOMATED_THEN_MANUAL
}

/**
 * Generate a Carousel, a gallery with button on the side to swap between two images
 *
 * WARNING : Not yet implemented, signature may change.
 */
@Composable
fun Carousel(images: List<Image>) {
  TODO()
}
