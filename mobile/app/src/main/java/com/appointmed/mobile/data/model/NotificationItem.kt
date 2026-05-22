package com.appointmed.mobile.data.model

data class NotificationItem(
    val id: Long = 0,
    val title: String = "",
    val message: String = "",
    val type: String? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null
)
