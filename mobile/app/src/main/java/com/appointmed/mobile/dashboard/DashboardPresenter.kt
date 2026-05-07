package com.appointmed.mobile.dashboard

import android.content.Context
import com.appointmed.mobile.data.local.Prefs
import com.appointmed.mobile.data.model.Appointment
import com.appointmed.mobile.data.model.DoctorResponse
import com.appointmed.mobile.data.network.ApiClient
import com.appointmed.mobile.data.network.ApiService
import com.appointmed.mobile.specialist.DoctorItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DashboardPresenter(
    private var view: DashboardContract.View?,
    private val context: Context
) : DashboardContract.Presenter {

    private val apiService: ApiService = ApiClient.create(context)
    private val prefs = Prefs(context)

    override fun checkLoginState() {
        if (!prefs.isLoggedIn()) {
            view?.navigateToLogin()
        } else {
            val user = prefs.getUser()
            val firstName = user.name.split(" ").firstOrNull() ?: user.name
            view?.showWelcomeName(firstName)
            view?.setDashboardViewType(user.role == "DOCTOR")
        }
    }

    override fun loadDashboardData() {
        val user = prefs.getUser()
        val userId = user.id
        if (userId == 0L) return

        if (user.role == "DOCTOR") {
            loadDoctorDashboard(userId)
        } else {
            loadPatientDashboard(userId)
        }
    }

    private fun loadPatientDashboard(userId: Long) {
        // Load upcoming appointments
        apiService.getPatientAppointments(userId).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                if (response.isSuccessful) {
                    val appointments = response.body() ?: emptyList()

                    // Find first upcoming (PENDING or CONFIRMED)
                    val upcoming = appointments
                        .filter { it.status == "PENDING" || it.status == "CONFIRMED" }
                        .sortedBy { it.appointmentDate }
                        .firstOrNull()

                    if (upcoming != null) {
                        val doctorName = upcoming.doctor?.user?.name ?: "Doctor"
                        val dateStr = formatDisplayDate(upcoming.appointmentDate)
                        val details = "$dateStr • ${upcoming.appointmentTime}"
                        val displayId = "AM-${upcoming.id}"
                        view?.showUpcomingAppointment(doctorName, details, displayId)
                    } else {
                        view?.hideUpcomingAppointment()
                    }
                }
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                // Silently fail
            }
        })

        // Load available doctors
        view?.showLoadingDoctors()
        apiService.getDoctors().enqueue(object : Callback<List<DoctorResponse>> {
            override fun onResponse(call: Call<List<DoctorResponse>>, response: Response<List<DoctorResponse>>) {
                view?.hideLoadingDoctors()
                if (response.isSuccessful) {
                    val doctors = response.body() ?: emptyList()
                    val doctorItems = doctors.map { it.toDoctorItem() }
                    view?.showAvailableDoctors(doctorItems)
                }
            }

            override fun onFailure(call: Call<List<DoctorResponse>>, t: Throwable) {
                view?.hideLoadingDoctors()
            }
        })
    }

    private fun loadDoctorDashboard(userId: Long) {
        view?.showLoadingDoctors()
        apiService.getDoctorAppointments(userId).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                view?.hideLoadingDoctors()
                if (response.isSuccessful) {
                    val appointments = response.body() ?: emptyList()
                    view?.showDoctorAppointments(appointments)
                    
                    // Show next appointment in top card
                    val next = appointments
                        .filter { it.status == "CONFIRMED" || it.status == "PENDING" }
                        .sortedBy { it.appointmentDate }
                        .firstOrNull()

                    if (next != null) {
                        val patientName = next.patient?.name ?: "Patient"
                        val dateStr = formatDisplayDate(next.appointmentDate)
                        val details = "$dateStr • ${next.appointmentTime}"
                        view?.showUpcomingAppointment(patientName, details, "AM-${next.id}")
                    } else {
                        view?.hideUpcomingAppointment()
                    }
                }
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                view?.hideLoadingDoctors()
            }
        })
    }

    private fun DoctorResponse.toDoctorItem(): DoctorItem {
        return DoctorItem(
            id = this.id,
            name = this.name,
            specialty = this.specialization,
            clinic = this.clinicAddress ?: "Clinic not specified",
            fee = this.consultationFee,
            rating = this.rating,
            available = this.available
        )
    }

    private fun formatDisplayDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr) ?: return dateStr
            
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
            val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
            
            val appointmentCal = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            when {
                isSameDay(today, appointmentCal) -> "Today"
                isSameDay(tomorrow, appointmentCal) -> "Tomorrow"
                isSameDay(yesterday, appointmentCal) -> "Yesterday"
                else -> outputFormat.format(date)
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    override fun onProfileClicked() {
        view?.navigateToProfile()
    }

    override fun onBookClicked() {
        view?.navigateToSelectSpecialist()
    }

    override fun onNotificationsClicked() {
        view?.navigateToNotifications()
    }

    override fun onSearchClicked() {
        view?.navigateToSelectSpecialist()
    }

    override fun onScheduleClicked() {
        view?.navigateToAppointments()
    }

    override fun onRecordsClicked() {
        view?.navigateToRecords()
    }

    override fun onDestroy() {
        view = null
    }
}
