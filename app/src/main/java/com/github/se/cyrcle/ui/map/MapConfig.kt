package com.github.se.cyrcle.ui.map

import android.util.Log
import androidx.compose.runtime.Composable
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.zone.Zone
import com.mapbox.common.NetworkRestriction
import com.mapbox.common.TileRegionLoadOptions
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
    return MapStyle("mapbox://styles/seanprz/cm27wh9ff00jl01r21jz3hcb1")
  }

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

  fun downloadZone(zone: Zone) {
    val tileStore = TileStore.create()

    val offlineManager = OfflineManager()
    val tilesetDescriptor =
        offlineManager.createTilesetDescriptor(
            TilesetDescriptorOptions.Builder()
                .styleURI("mapbox://styles/seanprz/cm27wh9ff00jl01r21jz3hcb1")
                .minZoom(minZoom.toInt().toByte())
                .maxZoom(maxZoom.toInt().toByte())
                .build())
    // create a geometry for lausanne
    val GEOMETRY =
        Polygon.fromLngLats(
            listOf(
                listOf(
                    zone.boundingBox.southwest(),
                    zone.boundingBox.northeast(),
                )))

    tileStore.loadTileRegion(
        zone.uid,
        TileRegionLoadOptions.Builder()
            .geometry(GEOMETRY)
            .descriptors(listOf(tilesetDescriptor))
            .acceptExpired(false)
            .networkRestriction(NetworkRestriction.NONE)
            .build(),
        { progress ->
          // Handle progress updates
          Log.d("MapScreen", "Progress: $progress")
        }) { expected ->
          if (expected.isValue) {
            // Tile region download finishes successfully
            expected.value?.let { Log.d("ZoneSelection", "Tile region downloaded: $it") }
          }
          expected.error?.let { Log.e("ZoneSelection", "Tile region download error: $it") }
        }
  }

  fun deleteZone(zone: Zone) {
    val tileStore = TileStore.create()
    tileStore.removeTileRegion(zone.uid) { expected ->
      if (expected.isValue) {
        // Tile region removal finishes successfully
        Log.d("MapScreen", "Tile region removed")
      }
      expected.error?.let { Log.e("MapScreen", "Tile region removal error: $it") }
    }
  }
}
