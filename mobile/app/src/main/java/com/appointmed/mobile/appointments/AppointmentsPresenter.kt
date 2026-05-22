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

    private var allAppointments: List<Appointment> = emptyList()
    private var currentFilter: String = "All"

    override fun loadAppointments() {
        val user = prefs.getUser()
        if (user.id == 0L) {
            view?.showError("User not logged in")
            return
        }

        view?.showLoading()
        
        // Use No-Cache to force fresh data from server
        val call = if (user.role == "DOCTOR") {
            apiService.getDoctorByUserId(user.id).enqueue(object : Callback<com.appointmed.mobile.data.model.DoctorResponse> {
                override fun onResponse(call: Call<com.appointmed.mobile.data.model.DoctorResponse>, response: Response<com.appointmed.mobile.data.model.DoctorResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val doctorId = response.body()!!.id
                        apiService.getDoctorAppointments(doctorId).enqueue(object : Callback<List<Appointment>> {
                            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                                view?.hideLoading()
                                if (response.isSuccessful) {
                                    allAppointments = response.body() ?: emptyList()
                                    applyFilterAndShow()
                                } else {
                                    view?.showError("Failed to load appointments")
                                }
                            }
                            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                                view?.hideLoading()
                                view?.showError("Network error: ${t.message}")
                            }
                        })
                    } else {
                        view?.hideLoading()
                        view?.showError("Failed to fetch doctor profile")
                    }
                }
                override fun onFailure(call: Call<com.appointmed.mobile.data.model.DoctorResponse>, t: Throwable) {
                    view?.hideLoading()
                    view?.showError("Network error: ${t.message}")
                }
            })
            return
        } else {
            apiService.getPatientAppointments(user.id)
        }

        call.enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    allAppointments = response.body() ?: emptyList()
                    applyFilterAndShow()
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

    private fun applyFilterAndShow() {
        val filtered = if (currentFilter == "All") {
            allAppointments
        } else {
            allAppointments.filter { it.status.equals(currentFilter, ignoreCase = true) }
        }
        view?.showAppointments(filtered.sortedWith(compareByDescending<Appointment> { it.appointmentDate }.thenByDescending { it.appointmentTime }))
    }

    override fun filterAppointments(status: String) {
        currentFilter = status
        applyFilterAndShow()
    }

    override fun confirmAppointment(appointmentId: Long) {
        view?.showLoading()
        apiService.confirmAppointment(appointmentId).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.showError("Appointment confirmed")
                    loadAppointments()
                } else {
                    view?.showError("Failed to confirm")
                }
            }
            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error")
            }
        })
    }

    override fun completeAppointment(appointmentId: Long) {
        view?.showLoading()
        apiService.completeAppointment(appointmentId).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.showError("Appointment completed")
                    loadAppointments()
                } else {
                    view?.showError("Failed to complete")
                }
            }
            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error")
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
