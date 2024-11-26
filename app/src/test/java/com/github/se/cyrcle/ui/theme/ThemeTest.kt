package com.github.se.cyrcle.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemeTest {
  data class ColorProperty(val name: String, val color: Color)

  @Test
  fun testLightColorScheme() {
    val lightColorProperties =
        listOf(
            ColorProperty("Primary", Cerulean),
            ColorProperty("OnPrimary", White),
            ColorProperty("PrimaryContainer", Cerulean),
            ColorProperty("OnPrimaryContainer", White),
            ColorProperty("InversePrimary", invertColor(Cerulean)),
            ColorProperty("Secondary", YInMnBlue),
            ColorProperty("OnSecondary", White),
            ColorProperty("SecondaryContainer", YInMnBlue),
            ColorProperty("OnSecondaryContainer", White),
            ColorProperty("Tertiary", ShamrockGreen),
            ColorProperty("OnTertiary", White),
            ColorProperty("TertiaryContainer", ShamrockGreen),
            ColorProperty("OnTertiaryContainer", White),
            ColorProperty("Error", Red),
            ColorProperty("OnError", White),
            ColorProperty("ErrorContainer", Red),
            ColorProperty("OnErrorContainer", White),
            ColorProperty("Background", White),
            ColorProperty("OnBackground", Black),
            ColorProperty("Surface", White),
            ColorProperty("OnSurface", Cerulean),
            ColorProperty("SurfaceDim", CeruleanLow),
            ColorProperty("SurfaceBright", CeruleanHigh),
            ColorProperty("SurfaceTint", Aero),
            ColorProperty("SurfaceVariant", Aero),
            ColorProperty("OnSurfaceVariant", White),
            ColorProperty("InverseSurface", invertColor(Cerulean)),
            ColorProperty("InverseOnSurface", invertColor(White)),
            ColorProperty("SurfaceContainer", Cerulean),
            ColorProperty("SurfaceContainerLowest", CeruleanLowest),
            ColorProperty("SurfaceContainerLow", CeruleanLow),
            ColorProperty("SurfaceContainerHigh", CeruleanHigh),
            ColorProperty("SurfaceContainerHighest", CeruleanHighest),
            ColorProperty("Scrim", Color.Unspecified),
            ColorProperty("Outline", Cerulean),
            ColorProperty("OutlineVariant", Color.Unspecified))
    lightColorProperties.forEach { colorProperty ->
      val expectedColor =
          when (colorProperty.name) {
            "Primary" -> Cerulean
            "OnPrimary" -> White
            "PrimaryContainer" -> Cerulean
            "OnPrimaryContainer" -> White
            "InversePrimary" -> invertColor(Cerulean)
            "Secondary" -> YInMnBlue
            "OnSecondary" -> White
            "SecondaryContainer" -> YInMnBlue
            "OnSecondaryContainer" -> White
            "Tertiary" -> ShamrockGreen
            "OnTertiary" -> White
            "TertiaryContainer" -> ShamrockGreen
            "OnTertiaryContainer" -> White
            "Error" -> Red
            "OnError" -> White
            "ErrorContainer" -> Red
            "OnErrorContainer" -> White
            "Background" -> White
            "OnBackground" -> Black
            "Surface" -> White
            "OnSurface" -> Cerulean
            "SurfaceDim" -> CeruleanLow
            "SurfaceBright" -> CeruleanHigh
            "SurfaceTint" -> Aero
            "SurfaceVariant" -> Aero
            "OnSurfaceVariant" -> White
            "InverseSurface" -> invertColor(Cerulean)
            "InverseOnSurface" -> invertColor(White)
            "SurfaceContainer" -> Cerulean
            "SurfaceContainerLowest" -> CeruleanLowest
            "SurfaceContainerLow" -> CeruleanLow
            "SurfaceContainerHigh" -> CeruleanHigh
            "SurfaceContainerHighest" -> CeruleanHighest
            "Scrim" -> Color.Unspecified
            "Outline" -> Cerulean
            "OutlineVariant" -> Color.Unspecified
            else -> throw IllegalArgumentException("Unknown color property: ${colorProperty.name}")
          }
      assertEquals("Color mismatch for ${colorProperty.name}", expectedColor, colorProperty.color)
    }
  }

  /* This test was commented, as it is the same as light theme for the moment. */
  //  @Test
  //  fun testDarkColorScheme() {
  //    val darkColorProperties =
  //        listOf(
  //            ColorProperty("Primary", DarkCerulean),
  //            ColorProperty("OnPrimary", White),
  //            ColorProperty(
  //                "PrimaryContainer", DarkCerulean),
  //            ColorProperty("OnPrimaryContainer", White),
  //            ColorProperty("InversePrimary", invertColor(DarkCerulean)),
  //            ColorProperty("Secondary", DarkMadder),
  //            ColorProperty("OnSecondary", White),
  //            ColorProperty(
  //                "SecondaryContainer", DarkMadder),
  //            ColorProperty("OnSecondaryContainer", White),
  //            ColorProperty("Tertiary", DarkGoldenBrown),
  //            ColorProperty("OnTertiary", White),
  //            ColorProperty(
  //                "TertiaryContainer", DarkGoldenBrown),
  //            ColorProperty("OnTertiaryContainer", White),
  //            ColorProperty("Error", DarkRed),
  //            ColorProperty("OnError", White),
  //            ColorProperty("ErrorContainer", DarkRed),
  //            ColorProperty("OnErrorContainer", White),
  //            ColorProperty("Background", Color(0xFF0A2530)),
  //            ColorProperty("OnBackground", White),
  //            ColorProperty("Surface", DarkCerulean),
  //            ColorProperty("OnSurface", White),
  //            ColorProperty("SurfaceDim", DarkCeruleanLow),
  //            ColorProperty("SurfaceBright", DarkCeruleanHigh),
  //            ColorProperty("SurfaceTint", DarkCeruleanTint),
  //            ColorProperty("SurfaceVariant", DarkBlue),
  //            ColorProperty("OnSurfaceVariant", White),
  //            ColorProperty("InverseSurface", invertColor(DarkCerulean)),
  //            ColorProperty("InverseOnSurface", invertColor(Black)),
  //            ColorProperty("SurfaceContainer", DarkCerulean),
  //            ColorProperty(
  //                "SurfaceContainerLowest",
  //                DarkCeruleanLowest),
  //            ColorProperty(
  //                "SurfaceContainerLow", DarkCeruleanLow),
  //            ColorProperty(
  //                "SurfaceContainerHigh",
  //                DarkCeruleanHigh),
  //            ColorProperty(
  //                "SurfaceContainerHighest",
  //                DarkCeruleanHighest),
  //            ColorProperty("Scrim", White),
  //            ColorProperty("Outline", White),
  //            ColorProperty("OutlineVariant", White))
  //
  //    darkColorProperties.forEach { colorProperty ->
  //      val expectedColor =
  //          when (colorProperty.name) {
  //            "Primary" -> DarkCerulean
  //            "OnPrimary" -> White
  //            "PrimaryContainer" -> DarkCerulean
  //            "OnPrimaryContainer" -> White
  //            "InversePrimary" -> invertColor(DarkCerulean)
  //            "Secondary" -> DarkMadder
  //            "OnSecondary" -> White
  //            "SecondaryContainer" -> DarkMadder
  //            "OnSecondaryContainer" -> White
  //            "Tertiary" -> DarkGoldenBrown
  //            "OnTertiary" -> White
  //            "TertiaryContainer" -> DarkGoldenBrown
  //            "OnTertiaryContainer" -> White
  //            "Error" -> DarkRed
  //            "OnError" -> White
  //            "ErrorContainer" -> DarkRed
  //            "OnErrorContainer" -> White
  //            "Background" -> Color(0xFF0A2530)
  //            "OnBackground" -> White
  //            "Surface" -> DarkCerulean
  //            "OnSurface" -> White
  //            "SurfaceDim" -> DarkCeruleanLow
  //            "SurfaceBright" -> DarkCeruleanHigh
  //            "SurfaceTint" -> DarkCeruleanTint
  //            "SurfaceVariant" -> DarkBlue
  //            "OnSurfaceVariant" -> White
  //            "InverseSurface" -> invertColor(DarkCerulean)
  //            "InverseOnSurface" -> invertColor(Black)
  //            "SurfaceContainer" -> DarkCerulean
  //            "SurfaceContainerLowest" ->
  //                DarkCeruleanLowest
  //            "SurfaceContainerLow" -> DarkCeruleanLow
  //            "SurfaceContainerHigh" -> DarkCeruleanHigh
  //            "SurfaceContainerHighest" ->
  //                DarkCeruleanHighest
  //            "Scrim" -> White
  //            "Outline" -> White
  //            "OutlineVariant" -> White
  //            else -> throw IllegalArgumentException("Unknown color property:
  // ${colorProperty.name}")
  //          }
  //      assertEquals("Color mismatch for ${colorProperty.name}", expectedColor,
  // colorProperty.color)
  //    }
  //  }
}
