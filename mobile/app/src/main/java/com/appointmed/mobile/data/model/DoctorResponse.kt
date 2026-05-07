package com.appointmed.mobile.data.model

data class DoctorResponse(
    val id: Long = 0,
    val userId: Long = 0,
    val name: String = "",
    val email: String = "",
    val specialization: String = "",
    val licenseNumber: String? = null,
    val phone: String? = null,
    val clinicAddress: String? = null,
    val avatarUrl: String? = null,
    val available: Boolean = true,
    val rating: Double = 0.0,
    val reviews: Int = 0,
    val consultationFee: Double = 0.0
)
