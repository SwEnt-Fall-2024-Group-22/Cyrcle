package com.github.se.cyrcle.model.parking.online

import android.util.Log
import com.github.se.cyrcle.io.serializer.ParkingAdapter
import com.github.se.cyrcle.model.parking.ImageReport
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.Tile
import com.github.se.cyrcle.model.parking.TileUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.*
import javax.inject.Inject

class ParkingRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    ParkingRepository {

  private val collectionPath = "parkings"
  private val parkingSerializer = ParkingAdapter()

  private var nbrOfRequest = 0

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

  override fun getParkingById(
      id: String,
      onSuccess: (Parking) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    nbrOfRequest += 1
    Log.d("parkingRepositoryFirestore", "nbrOfRequest: $nbrOfRequest")
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          val parking = document.data?.let(parkingSerializer::deserializeParking)
          if (parking != null) {
            onSuccess(parking)
          } else {
            onFailure(Exception("Parking not found"))
          }
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getParkingsByListOfIds(
      ids: List<String>,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (ids.isEmpty()) {
      onSuccess(emptyList())
      return
    }
    nbrOfRequest += 1
    Log.d("parkingRepositoryFirestore", "nbrOfRequest: $nbrOfRequest")
    db.collection(collectionPath)
        .whereIn("uid", ids)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val parkings =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let(parkingSerializer::deserializeParking)
              }
          onSuccess(parkings)
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getParkingsForTile(
      tile: Tile,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Get the bottom left and top right points of the tile
    // TODO This is a temporary solution for the time that `Parking.tile` gets added on Firestore
    val start = TileUtils.getBottomLeftPoint(tile)
    val end = TileUtils.getTopRightPoint(tile)
    if (start == null || end == null) {
      onFailure(Exception("Invalid tile"))
      return
    }
    if (start.latitude() > end.latitude() || start.longitude() > end.longitude()) {
      onFailure(Exception("Invalid range"))
      return
    }
    nbrOfRequest += 1
    Log.d("parkingRepositoryFirestore", "nbrOfRequest: $nbrOfRequest")
    db.collection(collectionPath)
        .whereGreaterThan("location.center.latitude", start.latitude())
        .whereLessThanOrEqualTo("location.center.latitude", end.latitude())
        .whereGreaterThan("location.center.longitude", start.longitude())
        .whereLessThanOrEqualTo("location.center.longitude", end.longitude())
        .get()
        .addOnSuccessListener { querySnapshot ->
          val parkings =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let(parkingSerializer::deserializeParking)
              }
          onSuccess(parkings)
        }
        .addOnFailureListener { e -> onFailure(e) }
  }

  override fun addParking(parking: Parking, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(parking.uid)
        .set(parkingSerializer.serializeParking(parking))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun updateParking(
      parking: Parking,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("ParkingRepositoryFirestore", "updateParking: $parking")
    db.collection(collectionPath)
        .document(parking.uid)
        .set(parkingSerializer.serializeParking(parking))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun deleteParkingById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val parkingDocRef = db.collection(collectionPath).document(id)
    val reportsCollectionRef = parkingDocRef.collection("reports")

    // Recursive function to delete all documents in a collection
    fun deleteSubcollection(
        collectionRef: CollectionReference,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
      collectionRef
          .get()
          .addOnSuccessListener { querySnapshot ->
            val batch = db.batch()

            for (document in querySnapshot.documents) {
              batch.delete(document.reference)
            }

            batch
                .commit()
                .addOnSuccessListener {
                  if (querySnapshot.size() < 500) {
                    // Fewer than 500 documents: subcollection is cleared
                    onComplete()
                  } else {
                    // More documents may exist; repeat the process
                    deleteSubcollection(collectionRef, onComplete, onError)
                  }
                }
                .addOnFailureListener(onError)
          }
          .addOnFailureListener(onError)
    }

    // Delete the reports subcollection first
    deleteSubcollection(
        reportsCollectionRef,
        onComplete = {
          // Once reports are deleted, delete the parking document
          parkingDocRef
              .delete()
              .addOnSuccessListener { onSuccess() }
              .addOnFailureListener { onFailure(it) }
        },
        onError = { exception ->
          onFailure(exception) // Handle errors during subcollection deletion
        })
  }

  override fun getReportsForParking(
      parkingId: String,
      onSuccess: (List<ParkingReport>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection("parkings")
        .document(parkingId)
        .collection("reports")
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reports =
              querySnapshot.documents.mapNotNull { document ->
                document.toObject(ParkingReport::class.java)
              }
          onSuccess(reports)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getReportsForImage(
      parkingId: String,
      imageId: String,
      onSuccess: (List<ImageReport>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection("parkings")
        .document(parkingId)
        .collection("image_reports")
        .whereEqualTo("image", imageId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reports =
              querySnapshot.documents.mapNotNull { document ->
                document.toObject(ImageReport::class.java)
              }
          onSuccess(reports)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun addReport(
      report: ParkingReport,
      onSuccess: (ParkingReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val reportId = getNewUid() // Generate a new unique ID for the report
    db.collection(collectionPath)
        .document(report.parking)
        .collection("reports")
        .document(reportId)
        .set(report)
        .addOnSuccessListener { onSuccess(report) }
        .addOnFailureListener { onFailure(it) }
  }

  override fun addImageReport(
      report: ImageReport,
      parking: String,
      onSuccess: (ImageReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val reportId = getNewUid() // Generate a new unique ID for the report
    db.collection(collectionPath)
        .document(parking) // Check if this is a valid parking ID
        .collection("image_reports")
        .document(reportId)
        .set(report)
        .addOnSuccessListener {
          Log.d("ParkingRepositoryFirestore", "Image report added: $reportId")
          onSuccess(report)
        }
        .addOnFailureListener {
          Log.e("ParkingRepositoryFirestore", "Failed to add image report: ${it.message}")
          onFailure(it)
        }
  }
}
