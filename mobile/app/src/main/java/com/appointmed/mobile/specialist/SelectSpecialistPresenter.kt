package com.appointmed.mobile.specialist

import android.content.Context
import com.appointmed.mobile.data.model.DoctorResponse
import com.appointmed.mobile.data.network.ApiClient
import com.appointmed.mobile.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectSpecialistPresenter(
    private var view: SelectSpecialistContract.View?,
    context: Context
) : SelectSpecialistContract.Presenter {

    private val apiService: ApiService = ApiClient.create(context)
    private var allDoctors: List<DoctorItem> = emptyList()
    private var currentFilter = "All"
    private var currentQuery = ""

    override fun loadDoctors() {
        view?.showLoading()
        apiService.getDoctors().enqueue(object : Callback<List<DoctorResponse>> {
            override fun onResponse(call: Call<List<DoctorResponse>>, response: Response<List<DoctorResponse>>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    val doctors = response.body() ?: emptyList()
                    allDoctors = doctors.map { it.toDoctorItem() }
                    applyLocalSearch()
                } else {
                    view?.showError("Failed to load doctors")
                }
            }

            override fun onFailure(call: Call<List<DoctorResponse>>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun loadSpecializations() {
        apiService.getSpecializations().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val specs = response.body() ?: emptyList()
                    view?.showSpecializations(specs)
                }
            }
            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                // Silently fail or log
            }
        })
    }

    override fun filterBySpecialty(specialty: String) {
        currentFilter = specialty
        if (specialty == "All") {
            // Reload all from API
            apiService.getDoctors().enqueue(object : Callback<List<DoctorResponse>> {
                override fun onResponse(call: Call<List<DoctorResponse>>, response: Response<List<DoctorResponse>>) {
                    if (response.isSuccessful) {
                        val doctors = response.body() ?: emptyList()
                        allDoctors = doctors.map { it.toDoctorItem() }
                        applyLocalSearch()
                    }
                }
                override fun onFailure(call: Call<List<DoctorResponse>>, t: Throwable) {
                    view?.showError("Network error: ${t.message}")
                }
            })
        } else {
            apiService.getDoctors(specialty).enqueue(object : Callback<List<DoctorResponse>> {
                override fun onResponse(call: Call<List<DoctorResponse>>, response: Response<List<DoctorResponse>>) {
                    if (response.isSuccessful) {
                        val doctors = response.body() ?: emptyList()
                        allDoctors = doctors.map { it.toDoctorItem() }
                        applyLocalSearch()
                    }
                }
                override fun onFailure(call: Call<List<DoctorResponse>>, t: Throwable) {
                    view?.showError("Network error: ${t.message}")
                }
            })
        }
    }

    override fun searchDoctors(query: String) {
        currentQuery = query
        applyLocalSearch()
    }

    private fun applyLocalSearch() {
        var filtered = allDoctors
        if (currentQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(currentQuery, ignoreCase = true) ||
                it.specialty.contains(currentQuery, ignoreCase = true)
            }
        }
        view?.showFilteredDoctors(filtered, filtered.size)
    }

    override fun onBookClicked(doctor: DoctorItem) {
        if (doctor.available) {
            view?.navigateToBooking(doctor)
        } else {
            view?.showError("This doctor is currently unavailable.")
        }
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
}
