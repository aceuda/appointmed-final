package com.appointmed.mobile.booking

interface BookingConfirmedContract {
    interface View {
        fun showConfirmationDetails(doctorName: String, specialty: String, date: String, time: String)
        fun navigateToDashboard()
        fun navigateToAppointments()
    }

    interface Presenter {
        fun loadDetails()
        fun onDashboardClicked()
        fun onAppointmentsClicked()
        fun onDestroy()
    }
}
