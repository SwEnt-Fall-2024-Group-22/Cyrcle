package com.github.se.cyrcle.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorSchemeTest {
  data class ColorProperty(val name: String, val color: Color)

  @Test
  fun testLightColorScheme() {
    val lightColorProperties =
        listOf(
            ColorProperty("Primary", Cerulean),
            ColorProperty("OnPrimary", White),
            ColorProperty("PrimaryContainer", Cerulean.copy(alpha = primaryContainerAlphaModifier)),
            ColorProperty("OnPrimaryContainer", White),
            ColorProperty("InversePrimary", Black),
            ColorProperty("Secondary", Madder),
            ColorProperty("OnSecondary", White),
            ColorProperty("SecondaryContainer", Madder.copy(alpha = primaryContainerAlphaModifier)),
            ColorProperty("OnSecondaryContainer", White),
            ColorProperty("Tertiary", GoldenBrown),
            ColorProperty("OnTertiary", White),
            ColorProperty(
                "TertiaryContainer", GoldenBrown.copy(alpha = primaryContainerAlphaModifier)),
            ColorProperty("OnTertiaryContainer", White),
            ColorProperty("Error", Red),
            ColorProperty("OnError", White),
            ColorProperty("ErrorContainer", Red.copy(alpha = primaryContainerAlphaModifier)),
            ColorProperty("OnErrorContainer", White),
            ColorProperty("Background", Cerulean.copy(alpha = 0.1f)),
            ColorProperty("OnBackground", Black),
            ColorProperty("Surface", Cerulean.copy(alpha = 0.5f)),
            ColorProperty("OnSurface", White),
            ColorProperty("SurfaceDim", CeruleanLow),
            ColorProperty("SurfaceBright", CeruleanHigh),
            ColorProperty("SurfaceTint", CeruleanTint),
            ColorProperty("SurfaceVariant", Blue.copy(alpha = 0.9f)),
            ColorProperty("OnSurfaceVariant", White),
            ColorProperty("InverseSurface", invertColor(Cerulean)),
            ColorProperty("InverseOnSurface", invertColor(White)),
            ColorProperty("SurfaceContainer", Cerulean.copy(alpha = 0.33f)),
            ColorProperty(
                "SurfaceContainerLowest",
                CeruleanLowest.copy(alpha = surfaceContainerAlphaModifier)),
            ColorProperty(
                "SurfaceContainerLow", CeruleanLow.copy(alpha = surfaceContainerAlphaModifier)),
            ColorProperty(
                "SurfaceContainerHigh", CeruleanHigh.copy(alpha = surfaceContainerAlphaModifier)),
            ColorProperty(
                "SurfaceContainerHighest",
                CeruleanHighest.copy(alpha = surfaceContainerAlphaModifier)),
            ColorProperty("Scrim", White),
            ColorProperty("Outline", White),
            ColorProperty("OutlineVariant", White))
    lightColorProperties.forEach { colorProperty ->
      val expectedColor =
          when (colorProperty.name) {
            "Primary" -> Cerulean
            "OnPrimary" -> White
            "PrimaryContainer" -> Cerulean.copy(alpha = primaryContainerAlphaModifier)
            "OnPrimaryContainer" -> White
            "InversePrimary" -> Black
            "Secondary" -> Madder
            "OnSecondary" -> White
            "SecondaryContainer" -> Madder.copy(alpha = primaryContainerAlphaModifier)
            "OnSecondaryContainer" -> White
            "Tertiary" -> GoldenBrown
            "OnTertiary" -> White
            "TertiaryContainer" -> GoldenBrown.copy(alpha = primaryContainerAlphaModifier)
            "OnTertiaryContainer" -> White
            "Error" -> Red
            "OnError" -> White
            "ErrorContainer" -> Red.copy(alpha = primaryContainerAlphaModifier)
            "OnErrorContainer" -> White
            "Background" -> Cerulean.copy(alpha = 0.1f)
            "OnBackground" -> Black
            "Surface" -> Cerulean.copy(alpha = 0.5f)
            "OnSurface" -> White
            "SurfaceDim" -> CeruleanLow
            "SurfaceBright" -> CeruleanHigh
            "SurfaceTint" -> CeruleanTint
            "SurfaceVariant" -> Blue.copy(alpha = 0.9f)
            "OnSurfaceVariant" -> White
            "InverseSurface" -> invertColor(Cerulean)
            "InverseOnSurface" -> invertColor(White)
            "SurfaceContainer" -> Cerulean.copy(alpha = 0.33f)
            "SurfaceContainerLowest" -> CeruleanLowest.copy(alpha = surfaceContainerAlphaModifier)
            "SurfaceContainerLow" -> CeruleanLow.copy(alpha = surfaceContainerAlphaModifier)
            "SurfaceContainerHigh" -> CeruleanHigh.copy(alpha = surfaceContainerAlphaModifier)
            "SurfaceContainerHighest" -> CeruleanHighest.copy(alpha = surfaceContainerAlphaModifier)
            "Scrim" -> White
            "Outline" -> White
            "OutlineVariant" -> White
            else -> throw IllegalArgumentException("Unknown color property: ${colorProperty.name}")
          }
      assertEquals("Color mismatch for ${colorProperty.name}", expectedColor, colorProperty.color)
    }
  }

  @Test
  fun testDarkColorScheme() {
    // TODO the dark them and copy it here after.
    val darkColorProperties =
        listOf(
            ColorProperty("Primary", Cerulean),
            ColorProperty("OnPrimary", White),
            ColorProperty("PrimaryContainer", Cerulean.copy(alpha = primaryContainerAlphaModifier)),
            ColorProperty("OnPrimaryContainer", White),
            ColorProperty("InversePrimary", Black),
            ColorProperty("Secondary", Madder),
            ColorProperty("OnSecondary", White),
            ColorProperty("SecondaryContainer", Madder.copy(alpha = primaryContainerAlphaModifier)),
            ColorProperty("OnSecondaryContainer", White),
            ColorProperty("Tertiary", GoldenBrown),
            ColorProperty("OnTertiary", White),
            ColorProperty(
                "TertiaryContainer", GoldenBrown.copy(alpha = primaryContainerAlphaModifier)),
            ColorProperty("OnTertiaryContainer", White),
            ColorProperty("Error", Red),
            ColorProperty("OnError", White),
            ColorProperty("ErrorContainer", Red.copy(alpha = primaryContainerAlphaModifier)),
            ColorProperty("OnErrorContainer", White),
            ColorProperty("Background", Cerulean.copy(alpha = 0.1f)),
            ColorProperty("OnBackground", Black),
            ColorProperty("Surface", Cerulean.copy(alpha = 0.5f)),
            ColorProperty("OnSurface", White),
            ColorProperty("SurfaceDim", CeruleanLow),
            ColorProperty("SurfaceBright", CeruleanHigh),
            ColorProperty("SurfaceTint", CeruleanTint),
            ColorProperty("SurfaceVariant", Blue.copy(alpha = 0.9f)),
            ColorProperty("OnSurfaceVariant", White),
            ColorProperty("InverseSurface", invertColor(Cerulean)),
            ColorProperty("InverseOnSurface", invertColor(White)),
            ColorProperty("SurfaceContainer", Cerulean.copy(alpha = 0.33f)),
            ColorProperty(
                "SurfaceContainerLowest",
                CeruleanLowest.copy(alpha = surfaceContainerAlphaModifier)),
            ColorProperty(
                "SurfaceContainerLow", CeruleanLow.copy(alpha = surfaceContainerAlphaModifier)),
            ColorProperty(
                "SurfaceContainerHigh", CeruleanHigh.copy(alpha = surfaceContainerAlphaModifier)),
            ColorProperty(
                "SurfaceContainerHighest",
                CeruleanHighest.copy(alpha = surfaceContainerAlphaModifier)),
            ColorProperty("Scrim", White),
            ColorProperty("Outline", White),
            ColorProperty("OutlineVariant", White))

    darkColorProperties.forEach { colorProperty ->
      val expectedColor =
          when (colorProperty.name) {
            "Primary" -> Cerulean
            "OnPrimary" -> White
            "PrimaryContainer" -> Cerulean.copy(alpha = primaryContainerAlphaModifier)
            "OnPrimaryContainer" -> White
            "InversePrimary" -> Black
            "Secondary" -> Madder
            "OnSecondary" -> White
            "SecondaryContainer" -> Madder.copy(alpha = primaryContainerAlphaModifier)
            "OnSecondaryContainer" -> White
            "Tertiary" -> GoldenBrown
            "OnTertiary" -> White
            "TertiaryContainer" -> GoldenBrown.copy(alpha = primaryContainerAlphaModifier)
            "OnTertiaryContainer" -> White
            "Error" -> Red
            "OnError" -> White
            "ErrorContainer" -> Red.copy(alpha = primaryContainerAlphaModifier)
            "OnErrorContainer" -> White
            "Background" -> Cerulean.copy(alpha = 0.1f)
            "OnBackground" -> Black
            "Surface" -> Cerulean.copy(alpha = 0.5f)
            "OnSurface" -> White
            "SurfaceDim" -> CeruleanLow
            "SurfaceBright" -> CeruleanHigh
            "SurfaceTint" -> CeruleanTint
            "SurfaceVariant" -> Blue.copy(alpha = 0.9f)
            "OnSurfaceVariant" -> White
            "InverseSurface" -> invertColor(Cerulean)
            "InverseOnSurface" -> invertColor(White)
            "SurfaceContainer" -> Cerulean.copy(alpha = 0.33f)
            "SurfaceContainerLowest" -> CeruleanLowest.copy(alpha = surfaceContainerAlphaModifier)
            "SurfaceContainerLow" -> CeruleanLow.copy(alpha = surfaceContainerAlphaModifier)
            "SurfaceContainerHigh" -> CeruleanHigh.copy(alpha = surfaceContainerAlphaModifier)
            "SurfaceContainerHighest" -> CeruleanHighest.copy(alpha = surfaceContainerAlphaModifier)
            "Scrim" -> White
            "Outline" -> White
            "OutlineVariant" -> White
            else -> throw IllegalArgumentException("Unknown color property: ${colorProperty.name}")
          }
      assertEquals("Color mismatch for ${colorProperty.name}", expectedColor, colorProperty.color)
    }
  }
}
