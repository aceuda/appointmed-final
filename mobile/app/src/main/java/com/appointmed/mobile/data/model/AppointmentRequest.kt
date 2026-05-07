package com.appointmed.mobile.data.model

data class AppointmentRequest(
    val patientId: Long,
    val doctorId: Long,
    val appointmentDate: String,  // yyyy-MM-dd
    val appointmentTime: String,  // HH:mm
    val endTime: String? = null,
    val reason: String? = null,
    val fee: Double? = null
)
