package com.appointmed.mobile.data.model

data class AuthResponse(
    val token: String? = null,
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
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
) {
    fun toUser(password: String? = null): User {
        return User(
            id = id,
            name = name,
            email = email,
            password = password ?: "",
            role = role,
            avatarUrl = avatarUrl,
            avatarData = avatarData,
            phone = phone,
            address = address,
            gender = gender,
            birthDate = birthDate,
            bloodType = bloodType,
            specialization = specialization,
            licenseNumber = licenseNumber,
            clinicAddress = clinicAddress,
            consultationFee = consultationFee
        )
    }
}
