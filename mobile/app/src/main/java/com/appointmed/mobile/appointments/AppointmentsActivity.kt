package com.appointmed.mobile.appointments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appointmed.mobile.R
import com.appointmed.mobile.data.model.Appointment
import com.appointmed.mobile.dashboard.DashboardActivity
import com.appointmed.mobile.profile.ProfileActivity
import com.appointmed.mobile.specialist.SelectSpecialistActivity
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsActivity : AppCompatActivity(), AppointmentsContract.View {

    private lateinit var presenter: AppointmentsContract.Presenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoAppointments: TextView
    private lateinit var adapter: AppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments)

        presenter = AppointmentsPresenter(this, this)

        recyclerView = findViewById(R.id.recyclerAppointments)
        progressBar = findViewById(R.id.progressBar)
        textNoAppointments = findViewById(R.id.textNoAppointments)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AppointmentAdapter(emptyList())
        recyclerView.adapter = adapter

        // Top bar
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Bottom Nav
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { presenter.onHomeClicked() }
        findViewById<LinearLayout>(R.id.navBook).setOnClickListener { presenter.onBookClicked() }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { presenter.onProfileClicked() }

        presenter.loadAppointments()
    }

    override fun showAppointments(appointments: List<Appointment>) {
        if (appointments.isEmpty()) {
            textNoAppointments.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            textNoAppointments.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(appointments)
        }
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    override fun navigateToSelectSpecialist() {
        startActivity(Intent(this, SelectSpecialistActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    // --- Adapter ---
    inner class AppointmentAdapter(
        private var items: List<Appointment>
    ) : RecyclerView.Adapter<AppointmentAdapter.VH>() {

        fun updateData(newItems: List<Appointment>) {
            items = newItems
            notifyDataSetChanged()
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val doctorName: TextView = view.findViewById(R.id.textDoctorName)
            val specialty: TextView = view.findViewById(R.id.textSpecialty)
            val dateTime: TextView = view.findViewById(R.id.textDateTime)
            val amount: TextView = view.findViewById(R.id.textAmount)
            val status: TextView = view.findViewById(R.id.textStatus)
            val btnCancel: View = view.findViewById(R.id.btnCancelAppointment)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = items[position]
            holder.doctorName.text = app.doctor?.user?.name ?: "Doctor"
            holder.specialty.text = app.doctor?.specialization ?: "Specialist"
            
            val displayDate = formatDisplayDate(app.appointmentDate)
            holder.dateTime.text = "$displayDate • ${app.appointmentTime}"
            
            holder.amount.text = "₱${String.format("%,.2f", app.fee)}"
            holder.status.text = app.status
            updateStatusStyle(holder.status, app.status)

            if (app.status == "PENDING" || app.status == "CONFIRMED") {
                holder.btnCancel.visibility = View.VISIBLE
                holder.btnCancel.setOnClickListener {
                    androidx.appcompat.app.AlertDialog.Builder(this@AppointmentsActivity)
                        .setTitle("Cancel Appointment")
                        .setMessage("Are you sure you want to cancel this appointment?")
                        .setPositiveButton("Yes") { _, _ ->
                            presenter.cancelAppointment(app.id)
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            } else {
                holder.btnCancel.visibility = View.GONE
            }
        }

        private fun formatDisplayDate(dateStr: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                if (date != null) outputFormat.format(date) else dateStr
            } catch (e: Exception) {
                dateStr
            }
        }

        private fun updateStatusStyle(view: TextView, status: String) {
            when (status) {
                "CONFIRMED" -> view.setBackgroundResource(R.drawable.bg_chip_active)
                "PENDING" -> view.setBackgroundResource(R.drawable.bg_chip_inactive)
                "CANCELLED" -> view.setBackgroundResource(R.drawable.bg_logout_button)
                else -> view.setBackgroundResource(R.drawable.bg_chip_inactive)
            }
            view.setTextColor(Color.WHITE)
        }

        override fun getItemCount() = items.size
    }
}
