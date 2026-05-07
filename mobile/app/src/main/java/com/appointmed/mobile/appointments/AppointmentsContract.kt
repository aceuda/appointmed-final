package com.appointmed.mobile.appointments

import com.appointmed.mobile.data.model.Appointment

interface AppointmentsContract {
    interface View {
        fun showAppointments(appointments: List<Appointment>)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun navigateToDashboard()
        fun navigateToProfile()
        fun navigateToSelectSpecialist()
    }

    interface Presenter {
        fun loadAppointments()
        fun cancelAppointment(appointmentId: Long)
        fun onHomeClicked()
        fun onProfileClicked()
        fun onBookClicked()
        fun onDestroy()
    }
}
