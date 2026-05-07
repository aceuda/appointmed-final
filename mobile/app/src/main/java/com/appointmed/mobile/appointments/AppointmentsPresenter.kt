package com.appointmed.mobile.appointments

import android.content.Context
import com.appointmed.mobile.data.local.Prefs
import com.appointmed.mobile.data.model.Appointment
import com.appointmed.mobile.data.network.ApiClient
import com.appointmed.mobile.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentsPresenter(
    private var view: AppointmentsContract.View?,
    private val context: Context
) : AppointmentsContract.Presenter {

    private val apiService: ApiService = ApiClient.create(context)
    private val prefs = Prefs(context)

    override fun loadAppointments() {
        val userId = prefs.getUser().id
        if (userId == 0L) {
            view?.showError("User not logged in")
            return
        }

        view?.showLoading()
        apiService.getPatientAppointments(userId).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    val appointments = response.body() ?: emptyList()
                    view?.showAppointments(appointments.sortedByDescending { it.appointmentDate })
                } else {
                    view?.showError("Failed to load appointments")
                }
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun onHomeClicked() {
        view?.navigateToDashboard()
    }

    override fun onProfileClicked() {
        view?.navigateToProfile()
    }

    override fun onBookClicked() {
        view?.navigateToSelectSpecialist()
    }

    override fun cancelAppointment(appointmentId: Long) {
        view?.showLoading()
        apiService.cancelAppointment(appointmentId).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.showError("Appointment cancelled successfully")
                    loadAppointments()
                } else {
                    view?.showError("Failed to cancel appointment")
                }
            }

            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun onDestroy() {
        view = null
    }
}
