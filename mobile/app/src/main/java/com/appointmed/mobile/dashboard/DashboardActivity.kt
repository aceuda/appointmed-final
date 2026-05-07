package com.appointmed.mobile.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appointmed.mobile.R
import com.appointmed.mobile.auth.LoginActivity
import com.appointmed.mobile.profile.ProfileActivity
import com.appointmed.mobile.specialist.DoctorItem
import com.appointmed.mobile.specialist.SelectSpecialistActivity

class DashboardActivity : AppCompatActivity(), DashboardContract.View {
    private lateinit var bottomNavProfile: LinearLayout
    private lateinit var buttonSearch: ImageButton
    private lateinit var buttonNotifications: ImageButton
    private lateinit var buttonViewDetails: Button
    private lateinit var quickBook: LinearLayout
    private lateinit var healthRecords: LinearLayout
    private lateinit var seeAllDoctors: TextView
    private lateinit var navDoctorsFAB: LinearLayout
    private lateinit var textDoctorName: TextView
    private lateinit var textAppointmentDetails: TextView
    private lateinit var textAppointmentId: TextView
    private lateinit var appointmentCard: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBarDoctors: ProgressBar
    private lateinit var adapter: DoctorAdapter
    private lateinit var appointmentAdapter: AppointmentAdapter
    private lateinit var textAvailableDoctorsHeader: TextView

    private lateinit var presenter: DashboardContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        presenter = DashboardPresenter(this, this)

        bottomNavProfile = findViewById(R.id.bottomNavProfile)
        buttonSearch = findViewById(R.id.buttonSearch)
        buttonNotifications = findViewById(R.id.buttonNotifications)
        buttonViewDetails = findViewById(R.id.buttonViewDetails)
        quickBook = findViewById(R.id.quickBook)
        healthRecords = findViewById(R.id.healthRecords)
        seeAllDoctors = findViewById(R.id.seeAllDoctors)
        navDoctorsFAB = findViewById(R.id.navDoctorsFAB)
        textDoctorName = findViewById(R.id.textDoctorName)
        textAppointmentDetails = findViewById(R.id.textAppointmentDetails)
        textAppointmentId = findViewById(R.id.textAppointmentId)
        textAvailableDoctorsHeader = findViewById(R.id.textAvailableDoctorsHeader)
        recyclerView = findViewById(R.id.recyclerAvailableDoctors)
        progressBarDoctors = findViewById(R.id.progressBarDoctors)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DoctorAdapter(emptyList()) { doctor ->
            // Use the same navigation as specialist selection
            val intent = Intent(this, com.appointmed.mobile.booking.BookAppointmentActivity::class.java).apply {
                putExtra("doctor_id", doctor.id)
                putExtra("doctor_name", doctor.name)
                putExtra("doctor_specialty", doctor.specialty)
                putExtra("doctor_fee", doctor.fee)
                putExtra("doctor_clinic", doctor.clinic)
            }
            startActivity(intent)
        }
        appointmentAdapter = AppointmentAdapter(emptyList()) { appointment ->
            Toast.makeText(this, "Opening appointment AM-${appointment.id}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // The appointment card is the parent of textDoctorName
        appointmentCard = textDoctorName.parent.parent as LinearLayout

        presenter.checkLoginState()

        bottomNavProfile.setOnClickListener { presenter.onProfileClicked() }
        buttonSearch.setOnClickListener { presenter.onSearchClicked() }
        buttonNotifications.setOnClickListener { presenter.onNotificationsClicked() }
        quickBook.setOnClickListener { presenter.onBookClicked() }
        seeAllDoctors.setOnClickListener { presenter.onBookClicked() }
        navDoctorsFAB.setOnClickListener { presenter.onBookClicked() }

        findViewById<View>(R.id.navHome).setOnClickListener { /* Already here */ }
        findViewById<View>(R.id.navSchedule).setOnClickListener { presenter.onScheduleClicked() }
        findViewById<View>(R.id.bottomNavProfile).setOnClickListener { presenter.onProfileClicked() }

        healthRecords.setOnClickListener { 
            presenter.onRecordsClicked()
        }
        buttonViewDetails.setOnClickListener {
            Toast.makeText(this, "Appointment details feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Load live data
        presenter.loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        presenter.checkLoginState()
        presenter.loadDashboardData()
    }

    override fun showUpcomingAppointment(doctorName: String, details: String, appointmentId: String) {
        appointmentCard.visibility = View.VISIBLE
        textDoctorName.text = doctorName
        textAppointmentDetails.text = details
        textAppointmentId.text = "ID: #$appointmentId"
    }

    override fun hideUpcomingAppointment() {
        textDoctorName.text = "No upcoming appointments"
        textAppointmentDetails.text = "Book a new appointment to get started"
    }

    override fun showWelcomeName(name: String) {
        // The brand text could be enhanced, but for now the dashboard 
        // uses the appointment card to show personalized data
    }

    override fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun navigateToSelectSpecialist() {
        startActivity(Intent(this, SelectSpecialistActivity::class.java))
    }

    override fun navigateToAppointments() {
        startActivity(Intent(this, com.appointmed.mobile.appointments.AppointmentsActivity::class.java))
    }

    override fun navigateToRecords() {
        Toast.makeText(this, "Health Records feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToNotifications() {
        Toast.makeText(this, "Notifications feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun showNotificationToast() {
        Toast.makeText(this, "Notifications feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun showSearchToast() {
        Toast.makeText(this, "Search feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun showAvailableDoctors(doctors: List<DoctorItem>) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.adapter = adapter
        // Limit to 3 or 5 for the dashboard
        adapter.updateData(doctors.take(5))
    }

    override fun showDoctorAppointments(appointments: List<com.appointmed.mobile.data.model.Appointment>) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.adapter = appointmentAdapter
        appointmentAdapter.updateData(appointments.take(5))
    }

    override fun setDashboardViewType(isDoctor: Boolean) {
        if (isDoctor) {
            textAvailableDoctorsHeader.text = "Your Appointments"
            quickBook.visibility = View.GONE
            healthRecords.visibility = View.GONE
            navDoctorsFAB.visibility = View.GONE
        } else {
            textAvailableDoctorsHeader.text = "Available Doctors"
            quickBook.visibility = View.VISIBLE
            healthRecords.visibility = View.VISIBLE
            navDoctorsFAB.visibility = View.VISIBLE
        }
    }

    override fun showLoadingDoctors() {
        progressBarDoctors.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    override fun hideLoadingDoctors() {
        progressBarDoctors.visibility = View.GONE
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    // ---- RecyclerView Adapter ----
    inner class DoctorAdapter(
        private var doctors: List<DoctorItem>,
        private val onBookClick: (DoctorItem) -> Unit
    ) : RecyclerView.Adapter<DoctorAdapter.VH>() {

        fun updateData(newData: List<DoctorItem>) {
            doctors = newData
            notifyDataSetChanged()
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.textDoctorName)
            val info: TextView = view.findViewById(R.id.textDoctorInfo)
            val rating: TextView = view.findViewById(R.id.textDoctorRating)
            val fee: TextView = view.findViewById(R.id.textDoctorFee)
            val btnBook: FrameLayout = view.findViewById(R.id.btnBookDoctor)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard_doctor, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val doc = doctors[position]
            holder.name.text = doc.name
            holder.info.text = doc.specialty
            holder.rating.text = " ${doc.rating}"
            holder.fee.text = " ₱${String.format("%,.0f", doc.fee)}"

            holder.btnBook.setOnClickListener { onBookClick(doc) }
            holder.itemView.setOnClickListener { onBookClick(doc) }
        }

        override fun getItemCount() = doctors.size
    }

    // ---- Appointment Adapter for Doctors ----
    inner class AppointmentAdapter(
        private var appointments: List<com.appointmed.mobile.data.model.Appointment>,
        private val onClick: (com.appointmed.mobile.data.model.Appointment) -> Unit
    ) : RecyclerView.Adapter<AppointmentAdapter.VH>() {

        fun updateData(newData: List<com.appointmed.mobile.data.model.Appointment>) {
            appointments = newData
            notifyDataSetChanged()
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.textDoctorName)
            val info: TextView = view.findViewById(R.id.textDoctorInfo)
            val rating: TextView = view.findViewById(R.id.textDoctorRating)
            val fee: TextView = view.findViewById(R.id.textDoctorFee)
            val btnBook: FrameLayout = view.findViewById(R.id.btnBookDoctor)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard_doctor, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = appointments[position]
            holder.name.text = app.patient?.name ?: "Unknown Patient"
            holder.info.text = "${app.appointmentDate} • ${app.appointmentTime}"
            holder.rating.text = " ${app.status}"
            
            // Hide the plus icon for appointments
            holder.btnBook.visibility = View.GONE
            
            holder.itemView.setOnClickListener { onClick(app) }
        }

        override fun getItemCount() = appointments.size
    }
}
