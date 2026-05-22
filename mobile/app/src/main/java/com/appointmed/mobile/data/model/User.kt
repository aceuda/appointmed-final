package com.appointmed.mobile.data.model

data class User(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "",
    val avatarUrl: String? = null,
    val avatarData: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val bloodType: String? = null,
    val specialization: String? = null,
    val licenseNumber: String? = null,
    val clinicAddress: String? = null,
    val consultationFee: Double = 0.0
)
