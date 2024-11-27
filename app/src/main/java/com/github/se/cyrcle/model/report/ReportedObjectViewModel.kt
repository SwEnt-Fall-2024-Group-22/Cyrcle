package com.github.se.cyrcle.model.report

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReportedObjectViewModel(private val reportedObjectRepository: ReportedObjectRepository) :
    ViewModel() {

  private val _selectedObject = MutableStateFlow<ReportedObject?>(null)
  val selectedObject: StateFlow<ReportedObject?> = _selectedObject

  fun getNewUid(): String {
    return reportedObjectRepository.getNewUid()
  }

  fun deleteReportedObject(uid: String) {
    reportedObjectRepository.deleteReportedObject(uid, {}, {})
  }

  fun selectObject(reportedObject: ReportedObject) {
    _selectedObject.value = reportedObject
  }
}
