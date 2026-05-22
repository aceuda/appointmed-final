package com.appointmed.mobile.booking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.appointmed.mobile.R
import com.appointmed.mobile.appointments.AppointmentsActivity
import com.appointmed.mobile.dashboard.DashboardActivity

class BookingConfirmedActivity : AppCompatActivity(), BookingConfirmedContract.View {

    private lateinit var presenter: BookingConfirmedContract.Presenter
    private lateinit var textConfirmDoctor: TextView
    private lateinit var textConfirmSpecialty: TextView
    private lateinit var textConfirmDate: TextView
    private lateinit var textConfirmTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_confirmed)

        val doctorName = intent.getStringExtra("doctor_name") ?: ""
        val doctorSpecialty = intent.getStringExtra("doctor_specialty") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val time = intent.getStringExtra("time") ?: ""

        presenter = BookingConfirmedPresenter(this, doctorName, doctorSpecialty, date, time)

        textConfirmDoctor = findViewById(R.id.textConfirmDoctor)
        textConfirmSpecialty = findViewById(R.id.textConfirmSpecialty)
        textConfirmDate = findViewById(R.id.textConfirmDate)
        textConfirmTime = findViewById(R.id.textConfirmTime)

        findViewById<Button>(R.id.btnViewAppointments).setOnClickListener {
            presenter.onAppointmentsClicked()
        }
        
        findViewById<Button>(R.id.btnGoDashboard).setOnClickListener {
            presenter.onDashboardClicked()
        }

        presenter.loadDetails()
    }

    override fun showConfirmationDetails(doctorName: String, specialty: String, date: String, time: String) {
        textConfirmDoctor.text = doctorName
        textConfirmSpecialty.text = specialty
        textConfirmDate.text = date
        textConfirmTime.text = time
    }

    override fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }

    override fun navigateToAppointments() {
        startActivity(Intent(this, AppointmentsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }

    override fun onDestroy() {
        if (::presenter.isInitialized) {
            presenter.onDestroy()
        }
        super.onDestroy()
    }
}
