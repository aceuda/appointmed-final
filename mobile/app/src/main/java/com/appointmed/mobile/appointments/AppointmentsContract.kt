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
        fun filterAppointments(status: String)
        fun confirmAppointment(appointmentId: Long)
        fun cancelAppointment(appointmentId: Long)
        fun completeAppointment(appointmentId: Long)
        fun onHomeClicked()
        fun onProfileClicked()
        fun onBookClicked()
        fun onDestroy()
    }
}
