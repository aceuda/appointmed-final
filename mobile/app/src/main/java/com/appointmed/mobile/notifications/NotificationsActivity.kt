package com.appointmed.mobile.notifications

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appointmed.mobile.R
import com.appointmed.mobile.appointments.AppointmentsActivity
import com.appointmed.mobile.data.model.NotificationItem
import com.appointmed.mobile.profile.ProfileActivity
import com.appointmed.mobile.dashboard.DashboardActivity
import com.appointmed.mobile.notifications.adapter.NotificationsAdapter
import com.appointmed.mobile.notifications.adapter.NotificationClickListener

class NotificationsActivity : AppCompatActivity(), NotificationsContract.View, NotificationClickListener {

    private lateinit var presenter: NotificationsContract.Presenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter
    private lateinit var progressOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        presenter = NotificationsPresenter(this, this)
        progressOverlay = findViewById(R.id.notificationsLoadingOverlay)
        recyclerView = findViewById(R.id.notificationsRecyclerView)
        adapter = NotificationsAdapter(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        findViewById<View>(R.id.navHome).setOnClickListener { presenter.onHomeClicked() }
        findViewById<View>(R.id.navProfile).setOnClickListener { presenter.onProfileClicked() }
        findViewById<View>(R.id.navAppointments).setOnClickListener { presenter.onScheduleClicked() }
        findViewById<View>(R.id.markAllReadButton).setOnClickListener { presenter.markAllAsRead() }

        presenter.loadNotifications()
    }

    override fun showNotifications(notifications: List<NotificationItem>) {
        adapter.submitList(notifications)
    }

    override fun showLoading() {
        progressOverlay.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressOverlay.visibility = View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    override fun navigateToAppointments() {
        startActivity(Intent(this, AppointmentsActivity::class.java))
        finish()
    }

    override fun onNotificationClicked(notification: NotificationItem) {
        presenter.markAsRead(notification.id)
    }

    override fun onDestroy() {
        if (::presenter.isInitialized) {
            presenter.onDestroy()
        }
        super.onDestroy()
    }
}
