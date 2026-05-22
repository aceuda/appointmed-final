package com.appointmed.mobile.notifications

import com.appointmed.mobile.data.model.NotificationItem

interface NotificationsContract {
    interface View {
        fun showNotifications(notifications: List<NotificationItem>)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun navigateToDashboard()
        fun navigateToProfile()
        fun navigateToAppointments()
    }

    interface Presenter {
        fun loadNotifications()
        fun markAsRead(notificationId: Long)
        fun markAllAsRead()
        fun onHomeClicked()
        fun onProfileClicked()
        fun onScheduleClicked()
        fun onDestroy()
    }
}
