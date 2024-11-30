package com.github.se.cyrcle.model.parking

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point
import javax.inject.Inject

class ParkingRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    ParkingRepository {

  private val collectionPath = "parkings"
  private val parkingSerializer = ParkingAdapter()

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
}
