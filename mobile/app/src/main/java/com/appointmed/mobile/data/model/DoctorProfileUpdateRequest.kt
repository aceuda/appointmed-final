package com.appointmed.mobile.data.model

data class DoctorProfileUpdateRequest(
    val specialization: String? = null,
    val licenseNumber: String? = null,
    val phone: String? = null,
    val clinicAddress: String? = null,
    val consultationFee: Double? = null
)
