package com.appointmed.mobile.dashboard

import com.appointmed.mobile.specialist.DoctorItem

interface DashboardContract {
    interface View {
        fun navigateToProfile()
        fun navigateToLogin()
        fun navigateToSelectSpecialist()
        fun navigateToAppointments()
        fun navigateToRecords()
        fun navigateToNotifications()
        fun showNotificationToast()
        fun showSearchToast()
        fun showUpcomingAppointment(doctorName: String, details: String, appointmentId: String)
        fun hideUpcomingAppointment()
        fun showWelcomeName(name: String)
        fun showAvailableDoctors(doctors: List<DoctorItem>)
        fun showDoctorAppointments(appointments: List<com.appointmed.mobile.data.model.Appointment>)
        fun showLoadingDoctors()
        fun hideLoadingDoctors()
        fun setDashboardViewType(isDoctor: Boolean)
    }

    interface Presenter {
        fun checkLoginState()
        fun loadDashboardData()
        fun onProfileClicked()
        fun onBookClicked()
        fun onNotificationsClicked()
        fun onSearchClicked()
        fun onScheduleClicked()
        fun onRecordsClicked()
        fun onDestroy()
    }
}
