package com.appointmed.mobile.data.model

data class User(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "",
    val avatarUrl: String? = null,
    val avatarData: String? = null,
    val consultationFee: Double = 0.0
)
