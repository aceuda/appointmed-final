package com.appointmed.mobile.notifications

import android.content.Context
import com.appointmed.mobile.data.local.Prefs
import com.appointmed.mobile.data.model.NotificationItem
import com.appointmed.mobile.data.network.ApiClient
import com.appointmed.mobile.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsPresenter(
    private var view: NotificationsContract.View?,
    context: Context
) : NotificationsContract.Presenter {

    private val apiService: ApiService = ApiClient.create(context)
    private val prefs = Prefs(context)

    override fun loadNotifications() {
        val userId = prefs.getUser().id
        if (userId == 0L) {
            view?.showError("Session expired. Please login again.")
            return
        }

        view?.showLoading()
        apiService.getNotifications(userId).enqueue(object : Callback<List<NotificationItem>> {
            override fun onResponse(call: Call<List<NotificationItem>>, response: Response<List<NotificationItem>>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.showNotifications(response.body() ?: emptyList())
                } else {
                    view?.showError("Unable to load notifications.")
                }
            }

            override fun onFailure(call: Call<List<NotificationItem>>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun markAsRead(notificationId: Long) {
        apiService.markNotificationRead(notificationId).enqueue(object : Callback<NotificationItem> {
            override fun onResponse(call: Call<NotificationItem>, response: Response<NotificationItem>) {
                if (response.isSuccessful) {
                    loadNotifications()
                }
            }

            override fun onFailure(call: Call<NotificationItem>, t: Throwable) {
                // ignore silently
            }
        })
    }

    override fun markAllAsRead() {
        val userId = prefs.getUser().id
        if (userId == 0L) return

        apiService.markAllNotificationsRead(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) loadNotifications()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // ignore silently
            }
        })
    }

    override fun onHomeClicked() {
        view?.navigateToDashboard()
    }

    override fun onProfileClicked() {
        view?.navigateToProfile()
    }

    override fun onScheduleClicked() {
        view?.navigateToAppointments()
    }

    override fun onDestroy() {
        view = null
    }
}
