package com.github.se.cyrcle.model.parking

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement

class ParkingRepositoryFirestore(private val db: FirebaseFirestore) : ParkingRepository {

  private val collectionPath = "parkings"

  override fun onSignIn(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun getParkings(onSuccess: (List<Parking>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val parkings =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeParking(it) }
              }
          onSuccess(parkings)
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getParkingById(
      id: String,
      onSuccess: (Parking) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          val parking = document.data?.let { deserializeParking(it) }
          if (parking != null) {
            onSuccess(parking)
          } else {
            onFailure(Exception("Parking not found"))
          }
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getParkingsBetween(
      start: Point,
      end: Point,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (start.latitude() > end.latitude() || start.longitude() > end.longitude()) {
      onFailure(Exception("Invalid range"))
      return
    }
    db.collection(collectionPath)
        .whereGreaterThanOrEqualTo("location.center.latitude", start.latitude())
        .whereLessThanOrEqualTo("location.center.latitude", end.latitude())
        .whereGreaterThanOrEqualTo("location.center.longitude", start.longitude())
        .whereLessThanOrEqualTo("location.center.longitude", end.longitude())
        .get()
        .addOnSuccessListener { querySnapshot ->
          val parkings =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeParking(it) }
              }
          onSuccess(parkings)
        }
        .addOnFailureListener { e -> onFailure(e) }
  }

  override fun getKClosestParkings(
      location: Point,
      k: Int,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // variables for radius search
    val initialRadius = 0.01
    val maxRadius = 0.5
    val step = 0.01

    var currentRadius = initialRadius
    var parkings = emptyList<Parking>()

    fun getMoreParkings() {
      getParkingsBetween(
          Point.fromLngLat(
              location.longitude() - currentRadius, location.latitude() - currentRadius),
          Point.fromLngLat(
              location.longitude() + currentRadius, location.latitude() + currentRadius),
          { newParkings ->
            parkings = (parkings + newParkings).distinctBy { it.uid }
            if (parkings.size < k && currentRadius < maxRadius) {
              currentRadius += step
              getMoreParkings()
            } else {
              onSuccess(
                  parkings
                      .sortedBy {
                        TurfMeasurement.distance(
                            location,
                            Point.fromLngLat(
                                it.location.center.longitude(), it.location.center.latitude()))
                      }
                      .take(k))
            }
          },
          onFailure)
    }

    getMoreParkings()
  }

  override fun addParking(parking: Parking, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(parking.uid)
        .set(serializeParking(parking))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun updateParking(
      parking: Parking,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(parking.uid)
        .set(serializeParking(parking))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun deleteParkingById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  private fun serializeParking(parking: Parking): Map<String, Any> {
    val gson = Gson()
    val type = object : TypeToken<Map<String, Any>>() {}.type
    val parkingMap: MutableMap<String, Any> = gson.fromJson(gson.toJson(parking), type)

    // Replace Mapbox Point with Point2D serialization
    val locationMap = (parkingMap["location"] as? Map<*, *>)?.toMutableMap()
    locationMap?.let { location ->
      location["center"] = Point2D.fromMapboxPoint(parking.location.center).toSerializedMap()
      parking.location.topLeft?.let { point ->
        location["topLeft"] = Point2D.fromMapboxPoint(point).toSerializedMap()
      }
      parking.location.topRight?.let { point ->
        location["topRight"] = Point2D.fromMapboxPoint(point).toSerializedMap()
      }
      parking.location.bottomLeft?.let { point ->
        location["bottomLeft"] = Point2D.fromMapboxPoint(point).toSerializedMap()
      }
      parking.location.bottomRight?.let { point ->
        location["bottomRight"] = Point2D.fromMapboxPoint(point).toSerializedMap()
      }
      parkingMap["location"] = location
    }
    return parkingMap
  }

  private fun deserializeParking(map: Map<String, Any>): Parking {
    val gson = Gson()
    val type = object : TypeToken<Map<String, Any>>() {}.type
    val parkingMap: MutableMap<String, Any> = gson.fromJson(gson.toJson(map), type)

    // Replace Point2D serialization with Mapbox Point
    val locationMap = (parkingMap["location"] as? Map<*, *>)?.toMutableMap()
    locationMap?.let { location ->
      location["center"] =
          Point2D.fromSerializedMap(location["center"] as Map<String, Double>).toMapboxPoint()
      location["topLeft"]?.let {
        location["topLeft"] = Point2D.fromSerializedMap(it as Map<String, Double>).toMapboxPoint()
      }
      location["topRight"]?.let {
        location["topRight"] = Point2D.fromSerializedMap(it as Map<String, Double>).toMapboxPoint()
      }
      location["bottomLeft"]?.let {
        location["bottomLeft"] =
            Point2D.fromSerializedMap(it as Map<String, Double>).toMapboxPoint()
      }
      location["bottomRight"]?.let {
        location["bottomRight"] =
            Point2D.fromSerializedMap(it as Map<String, Double>).toMapboxPoint()
      }

      parkingMap["location"] = location
    }

    return gson.fromJson(gson.toJson(parkingMap), Parking::class.java)
  }
}

private data class Point2D(val longitude: Double, val latitude: Double) {
  companion object {
    fun fromMapboxPoint(point: Point): Point2D {
      return Point2D(point.longitude(), point.latitude())
    }

    fun fromSerializedMap(map: Map<String, Double>): Point2D {
      return Point2D(map["longitude"]!!, map["latitude"]!!)
    }
  }

  fun toMapboxPoint(): Point {
    return Point.fromLngLat(longitude, latitude)
  }

  fun toSerializedMap(): Map<String, Any> {
    return Gson().fromJson(Gson().toJson(this), object : TypeToken<Map<String, Any>>() {}.type)
  }
}
