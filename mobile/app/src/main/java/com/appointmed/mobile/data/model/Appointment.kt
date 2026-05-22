package com.appointmed.mobile.data.model

data class Appointment(
    val id: Long = 0,
    val patient: AppointmentUser? = null,
    val doctor: AppointmentDoctor? = null,
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val endTime: String? = null,
    val reason: String? = null,
    val fee: Double = 0.0,
    val status: String = "PENDING"  // PENDING, CONFIRMED, COMPLETED, CANCELLED
)

data class AppointmentUser(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val avatarData: String? = null,
    val avatarUrl: String? = null
)

data class AppointmentDoctor(
    val id: Long = 0,
    val specialization: String = "",
    val phone: String? = null,
    val clinicAddress: String? = null,
    val avatarUrl: String? = null,
    val user: AppointmentUser? = null
)
