package com.github.se.cyrcle.model.report

import androidx.lifecycle.ViewModel
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.review.ReviewReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class Report {
  data class Parking(val parkingReport: ParkingReport) : Report()

  data class Review(val reviewReport: ReviewReport) : Report()
}

class ReportedObjectViewModel(private val reportedObjectRepository: ReportedObjectRepository) :
    ViewModel() {

  private val _selectedObject = MutableStateFlow<ReportedObject?>(null)
  val selectedObject: StateFlow<ReportedObject?> = _selectedObject

  val _reportsList = MutableStateFlow<List<ReportedObject>>(emptyList())
  val reportsList: StateFlow<List<ReportedObject>> = _reportsList

  fun getNewUid(): String {
    return reportedObjectRepository.getNewUid()
  }

  fun deleteReportedObject(uid: String) {
    reportedObjectRepository.deleteReportedObject(uid, {}, {})
  }

  fun selectObject(reportedObject: ReportedObject) {
    _selectedObject.value = reportedObject
  }

  fun clearSelectedObject() {
    _selectedObject.value = null
  }

  /** Fetches all reported objects from the repository and updates the reports list. */
  fun fetchAllReportedObjects() {
    reportedObjectRepository.getAllReportedObjects(
        onSuccess = { reportedObjects -> _reportsList.value = reportedObjects },
        onFailure = { exception ->
          println("Failed to fetch reported objects: ${exception.message}")
        })
  }
}
