package com.appointmed.mobile.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
    @SerializedName("address") val address: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("birthDate") val birthDate: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("licenseNumber") val licenseNumber: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("clinicAddress") val clinicAddress: String? = null,
    @SerializedName("consultationFee") val consultationFee: Int? = null
)
