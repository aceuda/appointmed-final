package com.appointmed.mobile.notifications.adapter

import com.appointmed.mobile.data.model.NotificationItem

interface NotificationClickListener {
    fun onNotificationClicked(notification: NotificationItem)
}
