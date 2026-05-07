package com.appointmed.mobile.data.model

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String,
    val address: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val specialization: String? = null,
    val licenseNumber: String? = null,
    val phone: String? = null,
    val clinicAddress: String? = null,
    val consultationFee: Int? = null
)
