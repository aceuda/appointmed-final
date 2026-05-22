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
            view?.showUserAvatar(user.avatarData)
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
                        .sortedWith(compareBy<Appointment> { it.appointmentDate }.thenBy { it.appointmentTime })
                        .firstOrNull()

                    if (upcoming != null) {
                        val doctorName = upcoming.doctor?.user?.name ?: "Doctor"
                        val dateStr = formatDisplayDate(upcoming.appointmentDate)
                        val details = "$dateStr • ${upcoming.appointmentTime}"
                        val displayId = "AM-${upcoming.id}"
                        val avatarData = upcoming.doctor?.user?.avatarData ?: upcoming.doctor?.avatarUrl
                        view?.showUpcomingAppointment(doctorName, details, displayId, avatarData)
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
        
        apiService.getDoctorByUserId(userId).enqueue(object : Callback<DoctorResponse> {
            override fun onResponse(call: Call<DoctorResponse>, response: Response<DoctorResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val doctorId = response.body()!!.id
                    val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val todayStr = todayFormat.format(Date())

                    // Fetch today's appointments
                    apiService.getDoctorAppointments(doctorId, todayStr).enqueue(object : Callback<List<Appointment>> {
                        override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                            if (response.isSuccessful) {
                                val appointments = (response.body() ?: emptyList())
                                    .sortedBy { it.appointmentTime }
                                view?.showDoctorDailyOverview(appointments)

                                // Fetch today's slots
                                apiService.getSlotsWithStatus(doctorId, todayStr).enqueue(object : Callback<List<com.appointmed.mobile.data.model.SlotStatus>> {
                                    override fun onResponse(call: Call<List<com.appointmed.mobile.data.model.SlotStatus>>, slotRes: Response<List<com.appointmed.mobile.data.model.SlotStatus>>) {
                                        view?.hideLoadingDoctors()
                                        if (slotRes.isSuccessful) {
                                            val slots = slotRes.body() ?: emptyList()
                                            view?.showDoctorSchedule(slots)

                                            // Compute stats
                                            val totalAppts = appointments.size
                                            val totalSlots = slots.size
                                            val bookedSlots = slots.count { it.status == "booked" }
                                            view?.showDoctorDashboardStats(totalAppts, bookedSlots, totalSlots)
                                        }
                                    }

                                    override fun onFailure(call: Call<List<com.appointmed.mobile.data.model.SlotStatus>>, t: Throwable) {
                                        view?.hideLoadingDoctors()
                                    }
                                })
                            }
                        }

                        override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                            view?.hideLoadingDoctors()
                        }
                    })
                } else {
                    view?.hideLoadingDoctors()
                }
            }
            override fun onFailure(call: Call<DoctorResponse>, t: Throwable) {
                view?.hideLoadingDoctors()
            }
        })
    }

    override fun confirmAppointment(id: Long) {
        apiService.confirmAppointment(id).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                if (response.isSuccessful) {
                    view?.showToast("Appointment confirmed!")
                    loadDashboardData() // Refresh to update overview and slots
                } else {
                    view?.showToast("Failed to confirm")
                }
            }
            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                view?.showToast("Network error")
            }
        })
    }

    override fun completeAppointment(id: Long) {
        apiService.completeAppointment(id).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                if (response.isSuccessful) {
                    view?.showToast("Appointment completed!")
                    loadDashboardData() // Refresh
                } else {
                    view?.showToast("Failed to complete")
                }
            }
            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                view?.showToast("Network error")
            }
        })
    }

    override fun toggleSlotAvailability(slot: com.appointmed.mobile.data.model.SlotStatus) {
        val user = prefs.getUser()
        apiService.getDoctorByUserId(user.id).enqueue(object : Callback<DoctorResponse> {
            override fun onResponse(call: Call<DoctorResponse>, response: Response<DoctorResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val doctorId = response.body()!!.id
                    val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val todayStr = todayFormat.format(Date())
                    
                    apiService.toggleSlot(doctorId, todayStr, slot.time).enqueue(object : Callback<Map<String, Any>> {
                        override fun onResponse(call: Call<Map<String, Any>>, res: Response<Map<String, Any>>) {
                            if (res.isSuccessful) {
                                val action = if (slot.status == "blocked") "unblocked" else "blocked"
                                view?.showToast("${slot.time} $action successfully")
                                loadDashboardData() // Refresh slots
                            } else {
                                view?.showToast("Failed to toggle slot")
                            }
                        }
                        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                            view?.showToast("Network error")
                        }
                    })
                }
            }
            override fun onFailure(call: Call<DoctorResponse>, t: Throwable) {
                view?.showToast("Network error")
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
            available = this.available,
            avatarUrl = this.avatarUrl
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
        val user = prefs.getUser()
        if (user.role == "DOCTOR") {
            view?.navigateToAppointments()
        } else {
            view?.navigateToSelectSpecialist()
        }
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
