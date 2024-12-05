package com.github.se.cyrcle.model.report

/**
 * Represents the reason for reporting an object. Can be one of the five parking reasons or one of
 * the 5 report reasons depending on nature of reported object
 */

/** Enum representing the type of object being reported. Can be either a parking or a review. */
enum class ReportedObjectType {
    PARKING,
    REVIEW,
    IMAGE
}

/**
 * Represents a reported object in the system, encapsulating the object details, the reason for the
 * report, and the user who submitted the report.
 *
 * @property objectUID The unique identifier of the reported object (e.g., parking or review).
 * @property reportUID The unique identifier of the report itself.
 * @property reason The reason for reporting the object, as a [ReportReason].
 * @property userUID The unique identifier of the user who owns the object being reported
 * @property objectType The type of the reported object, as a [ReportedObjectType].
 * @property reportTime The time that the object is reported.
 */
data class ReportedObject(
    val objectUID: String,
    val reportUID: String,
    var nbOfTimesReported: Int,
    var nbOfTimesMaxSeverityReported: Int,
    val userUID: String,
    val objectType: ReportedObjectType,
)
