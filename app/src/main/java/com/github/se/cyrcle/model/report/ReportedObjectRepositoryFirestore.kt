package com.github.se.cyrcle.model.report

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class ReportedObjectRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    ReportedObjectRepository {

  private val collectionPath = "reported_objects"

  private val gson: Gson = GsonBuilder().create()

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun addReportedObject(
      reportedObject: ReportedObject,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val reportData = serializeReportedObject(reportedObject)
    db.collection(collectionPath)
        .document(reportedObject.reportUID)
        .set(reportData)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getReportedObjectsByType(
      type: ReportedObjectType,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("objectType", type.name)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reportedObjects =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeReportedObject(it) }
              }
          onSuccess(reportedObjects)
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getReportedObjectsByObjectUID(
      objectUID: String,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("objectUID", objectUID)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reportedObjects =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeReportedObject(it) }
              }
          onSuccess(reportedObjects)
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getReportedObjectsByUser(
      userUID: String,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("userUID", userUID)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reportedObjects =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeReportedObject(it) }
              }
          onSuccess(reportedObjects)
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun deleteReportedObject(
      reportUID: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(reportUID)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  fun serializeReportedObject(reportedObject: ReportedObject): Map<String, Any> {
    val json = gson.toJson(reportedObject)
    val type = object : TypeToken<Map<String, Any>>() {}.type
    return gson.fromJson(json, type)
  }

  fun deserializeReportedObject(data: Map<String, Any>): ReportedObject {
    val json = gson.toJson(data)
    return gson.fromJson(json, ReportedObject::class.java)
  }
}
