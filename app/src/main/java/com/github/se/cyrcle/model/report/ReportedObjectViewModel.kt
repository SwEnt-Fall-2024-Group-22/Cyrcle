package com.github.se.cyrcle.model.report

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReportedObjectViewModel(
    private val reportedObjectRepository: ReportedObjectRepository,
) {

  /** Selected parking to review/edit */
  private val _selectedObject = MutableStateFlow<ReportedObject?>(null)
  val selectedParking: StateFlow<ReportedObject?> = _selectedObject

  fun getNewUid(): String {
    return reportedObjectRepository.getNewUid()
  }

  fun selectObject(obj: ReportedObject) {
    _selectedObject.value = obj
  }

  fun addReportedObject(obj: ReportedObject) {
    reportedObjectRepository.addReportedObject(obj, {}, {})
  }

  fun getObject(uid: String) {
    reportedObjectRepository.getReportedObjectsByObjectUID(uid, {}, {})
  }
}
