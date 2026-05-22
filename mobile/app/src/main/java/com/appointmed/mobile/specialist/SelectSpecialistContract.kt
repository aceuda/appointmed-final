package com.appointmed.mobile.specialist

data class DoctorItem(
    val id: Long,
    val name: String,
    val specialty: String,
    val clinic: String,
    val fee: Double,
    val rating: Double,
    val available: Boolean,
    val avatarUrl: String? = null
)

interface SelectSpecialistContract {
    interface View {
        fun showDoctors(doctors: List<DoctorItem>)
        fun showFilteredDoctors(doctors: List<DoctorItem>, count: Int)
        fun showSpecializations(specs: List<String>)
        fun showLoading()
        fun hideLoading()
        fun navigateToBooking(doctor: DoctorItem)
        fun navigateToDashboard()
        fun navigateToProfile()
        fun navigateToAppointments()
        fun showError(message: String)
    }

    interface Presenter {
        fun loadDoctors()
        fun loadSpecializations()
        fun filterBySpecialty(specialty: String)
        fun searchDoctors(query: String)
        fun onBookClicked(doctor: DoctorItem)
        fun onHomeClicked()
        fun onProfileClicked()
        fun onScheduleClicked()
        fun onDestroy()
    }
}
