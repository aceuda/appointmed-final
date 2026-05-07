package com.appointmed.mobile.data.model

data class StatsResponse(
    val totalAppointments: Int = 0,
    val upcoming: Int = 0,
    val completed: Int = 0,
    val cancelled: Int = 0
)
