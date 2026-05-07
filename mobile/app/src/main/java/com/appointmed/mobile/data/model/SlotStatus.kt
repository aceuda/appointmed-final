package com.appointmed.mobile.data.model

data class SlotStatus(
    val time: String = "",
    val status: String = "available", // available, booked, blocked
    val patientName: String? = null,
    val appointmentId: Long? = null,
    val appointmentStatus: String? = null
)
