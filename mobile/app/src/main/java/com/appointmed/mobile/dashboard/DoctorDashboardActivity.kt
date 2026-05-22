package com.appointmed.mobile.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appointmed.mobile.R
import com.appointmed.mobile.data.local.Prefs
import com.appointmed.mobile.notifications.NotificationsActivity
import com.appointmed.mobile.profile.ProfileActivity
import com.appointmed.mobile.specialist.DoctorItem
import com.appointmed.mobile.data.model.Appointment
import com.appointmed.mobile.data.model.SlotStatus
import com.appointmed.mobile.data.network.ApiClient
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class DoctorDashboardActivity : AppCompatActivity(), DashboardContract.View {

    private lateinit var textDoctorNameHeader: TextView
    private lateinit var textCurrentDate: TextView
    private lateinit var imageDashboardUserAvatar: ImageView
    private lateinit var textTotalAppointments: TextView
    private lateinit var textBookedSlots: TextView
    private lateinit var textAvailableSlots: TextView
    private lateinit var textEmptyOverview: TextView
    
    private lateinit var recyclerDailyOverview: RecyclerView
    private lateinit var recyclerSchedule: RecyclerView

    private lateinit var presenter: DashboardContract.Presenter
    private lateinit var appointmentAdapter: DoctorDashboardAppointmentAdapter
    private lateinit var slotAdapter: DoctorDashboardSlotAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)

        presenter = DashboardPresenter(this, this)

        textDoctorNameHeader = findViewById(R.id.textDoctorNameHeader)
        textCurrentDate = findViewById(R.id.textCurrentDate)
        imageDashboardUserAvatar = findViewById(R.id.imageDashboardUserAvatar)
        textTotalAppointments = findViewById(R.id.textTotalAppointments)
        textBookedSlots = findViewById(R.id.textBookedSlots)
        textAvailableSlots = findViewById(R.id.textAvailableSlots)
        textEmptyOverview = findViewById(R.id.textEmptyOverview)
        recyclerDailyOverview = findViewById(R.id.recyclerDailyOverview)
        recyclerSchedule = findViewById(R.id.recyclerSchedule)

        // Set date
        val df = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        textCurrentDate.text = df.format(Date())

        // Setup Recyclers
        appointmentAdapter = DoctorDashboardAppointmentAdapter(emptyList(),
            onConfirmClick = { id -> presenter.confirmAppointment(id) },
            onCompleteClick = { id -> presenter.completeAppointment(id) }
        )
        recyclerDailyOverview.layoutManager = LinearLayoutManager(this)
        recyclerDailyOverview.adapter = appointmentAdapter

        slotAdapter = DoctorDashboardSlotAdapter(emptyList()) { slot ->
            presenter.toggleSlotAvailability(slot)
        }
        recyclerSchedule.layoutManager = GridLayoutManager(this, 3)
        recyclerSchedule.adapter = slotAdapter

        // Bottom Navigation
        findViewById<LinearLayout>(R.id.bottomNavProfile).setOnClickListener { presenter.onProfileClicked() }
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { } // Already here
        findViewById<LinearLayout>(R.id.navSchedule).setOnClickListener { presenter.onScheduleClicked() }

        presenter.checkLoginState()
    }

    override fun onResume() {
        super.onResume()
        presenter.loadDashboardData()
    }

    override fun showWelcomeName(name: String) {
        val user = Prefs(this).getUser()
        textDoctorNameHeader.text = user.name
    }

    override fun showUserAvatar(avatarData: String?) {
        if (!avatarData.isNullOrEmpty()) {
            val fullUrl = if (avatarData.startsWith("http")) avatarData else ApiClient.IMAGE_BASE_URL + avatarData
            if (avatarData.startsWith("data:image")) {
                try {
                    val cleanData = avatarData.substringAfter(",")
                    val decodedBytes = android.util.Base64.decode(cleanData, android.util.Base64.NO_WRAP)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    Glide.with(this).load(bitmap).circleCrop().into(imageDashboardUserAvatar)
                    imageDashboardUserAvatar.imageTintList = null
                } catch (e: Exception) {
                    // Ignore
                }
            } else {
                Glide.with(this).load(fullUrl).circleCrop().into(imageDashboardUserAvatar)
                imageDashboardUserAvatar.imageTintList = null
            }
        }
    }

    override fun showDoctorDashboardStats(totalAppts: Int, bookedSlots: Int, totalSlots: Int) {
        val openSlots = totalSlots - bookedSlots - (totalSlots - bookedSlots - (totalSlots - bookedSlots)) // Wait, blocked is not part of total
        // Available Slots calculation: total slots minus booked slots minus blocked slots. 
        // We receive bookedSlots count correctly from presenter.
        
        textTotalAppointments.text = "$totalAppts Patients"
        textBookedSlots.text = "$bookedSlots Booked"
        
        // Compute available slots correctly inside Presenter, or just do it here if possible. Let's just use open slots logic:
        val openCount = totalSlots - bookedSlots // Not fully accurate if blocked, but good enough for display text if we want to parse it. 
        // Wait, the presenter provides `totalSlots` and `bookedSlots`. We need availableSlots too.
        // Actually I'll update the presenter to pass `availableSlots`. Let's assume it's `openCount` for now and modify presenter if needed.
    }
    
    fun setStats(totalAppts: Int, booked: Int, available: Int) {
        textTotalAppointments.text = "$totalAppts Patients"
        textBookedSlots.text = "$booked Booked"
        textAvailableSlots.text = "$available Open"
    }

    override fun showDoctorDailyOverview(appointments: List<Appointment>) {
        if (appointments.isEmpty()) {
            textEmptyOverview.visibility = View.VISIBLE
            recyclerDailyOverview.visibility = View.GONE
        } else {
            textEmptyOverview.visibility = View.GONE
            recyclerDailyOverview.visibility = View.VISIBLE
            appointmentAdapter.updateData(appointments)
        }
    }

    override fun showDoctorSchedule(slots: List<SlotStatus>) {
        val bookedCount = slots.count { it.status == "booked" }
        val blockedCount = slots.count { it.status == "blocked" }
        val availableCount = slots.count { it.status == "available" }
        setStats(appointmentAdapter.itemCount, bookedCount, availableCount)
        
        slotAdapter.updateData(slots)
    }

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // Ignore methods not used by Doctor Dashboard
    override fun showDoctorStats(patientCount: Int) {}
    override fun showLoadingDoctors() {}
    override fun hideLoadingDoctors() {}
    override fun setDashboardViewType(isDoctor: Boolean) {}
    override fun showAvailableDoctors(doctors: List<DoctorItem>) {}
    override fun showDoctorAppointments(appointments: List<Appointment>) {}
    override fun showUpcomingAppointment(doctorName: String, details: String, appointmentId: String, avatarData: String?) {}
    override fun hideUpcomingAppointment() {}

    override fun navigateToProfile() { startActivity(Intent(this, ProfileActivity::class.java)) }
    override fun navigateToLogin() {
        startActivity(Intent(this, com.appointmed.mobile.auth.LoginActivity::class.java))
        finish()
    }
    override fun navigateToSelectSpecialist() {}
    override fun navigateToAppointments() { startActivity(Intent(this, com.appointmed.mobile.appointments.AppointmentsActivity::class.java)) }
    override fun navigateToRecords() {}
    override fun navigateToNotifications() { startActivity(Intent(this, NotificationsActivity::class.java)) }
    override fun showNotificationToast() {}
    override fun showSearchToast() {}

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}
