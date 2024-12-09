package com.github.se.cyrcle.ui.map

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.zone.Zone
import com.mapbox.common.NetworkRestriction
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileRegionLoadProgress
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.OfflineManager
import com.mapbox.maps.TilesetDescriptorOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle

object MapConfig {
  private const val STYLE_URI = "mapbox://styles/seanprz/cm27wh9ff00jl01r21jz3hcb1"

  @Composable
  fun createMapViewPortStateFromViewModel(mapViewModel: MapViewModel): MapViewportState {
    return rememberMapViewportState {
      setCameraOptions {
        val camPos = mapViewModel.cameraPosition.value!!
        center(camPos.center)
        zoom(camPos.zoom)
        bearing(camPos.bearing)
        pitch(camPos.pitch)
      }
    }
  }

  fun defaultCameraState(): CameraState {
    return CameraState(
        Point.fromLngLat(6.566, 46.519), EdgeInsets(0.0, 0.0, 0.0, 0.0), 16.0, 0.0, 0.0)
  }

  @Composable
  fun DefaultStyle() {
    return MapStyle(STYLE_URI)
  }

  // Useful function to debug the downloaded tiles, can be called from mapScreen if needed
  fun getAllTilesDownloaded() {
    val tileStore = TileStore.create()
    // Get a list of tile regions that are currently available.
    tileStore.getAllTileRegions { expected ->
      if (expected.isValue) {
        expected.value?.let { tileRegionList ->
          Log.d("MapScreen", "Existing tile regions: $tileRegionList")
        }

        expected.error?.let { tileRegionError ->
          Log.e("MapScreen", "TileRegionError: $tileRegionError")
        }
      }
    }
  }

  /**
   * Download the tiles to local storage for a specific zone
   *
   * @param zone the zone to download
   * @param progressState the state to update the download progress (optional)
   */
  fun downloadZone(
      zone: Zone,
      progressState: MutableState<TileRegionLoadProgress?> = mutableStateOf(null)) {
    // Defines the area to download
    val geometry =
        Polygon.fromLngLats(
            listOf(
                listOf(
                    zone.boundingBox.southwest(),
                    zone.boundingBox.northeast(),
                )))

    // Define style and zooms levels to download
    val tilesetDescriptor =
        OfflineManager()
            .createTilesetDescriptor(
                TilesetDescriptorOptions.Builder()
                    .styleURI(STYLE_URI)
                    .minZoom(minZoom.toInt().toByte())
                    .maxZoom(maxZoom.toInt().toByte())
                    .build())

    // Finally Download the tile region
    TileStore.create().loadTileRegion(
        zone.uid, // identify the tileRegsion with the zoneUID
        TileRegionLoadOptions.Builder()
            .geometry(geometry)
            .descriptors(listOf(tilesetDescriptor))
            .acceptExpired(false)
            .networkRestriction(NetworkRestriction.NONE)
            .build(),
        { progress ->
          progressState.value = progress
          Log.d("MapScreen", "Progress: $progress")
        }) { expected ->
          if (expected.isValue) {
            // Tile region download finishes successfully
            expected.value?.let { Log.d("ZoneManager", "Tile region downloaded: $it") }
          }
          expected.error?.let { Log.e("ZoneManager", "Tile region download error: $it") }
        }
  }

  /**
   * Delete the tiles from local storage for a specific zone
   *
   * @param zone the zone to delete
   */
  fun deleteZoneFromStorage(zone: Zone) {
    TileStore.create().removeTileRegion(zone.uid) { expected ->
      if (expected.isValue) {
        // Tile region removal finishes successfully
        Log.d("ZoneManager", "Tile region ${zone.uid}  removed")
      }
      expected.error?.let { Log.e("ZoneManager", "Tile region removal error: $it") }
    }
  }
}
