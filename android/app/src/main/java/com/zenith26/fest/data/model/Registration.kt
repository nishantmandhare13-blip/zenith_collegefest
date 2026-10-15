package com.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

/**
 * Registration request payload sent to POST /api/events/{event_id}/register.
 */
data class RegistrationIn(
    @SerializedName("student_name")
    val studentName: String,

    @SerializedName("roll_number")
    val rollNumber: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String? = null
)

/**
 * Confirmation payload returned by POST /api/events/{event_id}/register on HTTP 201.
 */
data class RegistrationOut(
    @SerializedName("id")
    val id: Int,

    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("student_name")
    val studentName: String,

    @SerializedName("registered_at")
    val registeredAt: String
)

/**
 * Item in registration list returned by GET /api/registrations.
 */
data class RegistrationDetail(
    @SerializedName("id")
    val id: Int,

    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("event_title")
    val eventTitle: String,

    @SerializedName("student_name")
    val studentName: String,

    @SerializedName("roll_number")
    val rollNumber: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("registered_at")
    val registeredAt: String
)
