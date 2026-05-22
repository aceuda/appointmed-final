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
        fun showUpcomingAppointment(doctorName: String, details: String, appointmentId: String, avatarData: String?)
        fun hideUpcomingAppointment()
        fun showWelcomeName(name: String)
        fun showUserAvatar(avatarData: String?)
        fun showAvailableDoctors(doctors: List<DoctorItem>)
        fun showDoctorAppointments(appointments: List<com.appointmed.mobile.data.model.Appointment>)
        fun showDoctorStats(patientCount: Int)
        fun showLoadingDoctors()
        fun hideLoadingDoctors()
        fun setDashboardViewType(isDoctor: Boolean)
        fun showDoctorDashboardStats(totalAppts: Int, bookedSlots: Int, totalSlots: Int)
        fun showDoctorDailyOverview(appointments: List<com.appointmed.mobile.data.model.Appointment>)
        fun showDoctorSchedule(slots: List<com.appointmed.mobile.data.model.SlotStatus>)
        fun showToast(msg: String)
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
        fun confirmAppointment(id: Long)
        fun completeAppointment(id: Long)
        fun toggleSlotAvailability(slot: com.appointmed.mobile.data.model.SlotStatus)
        fun onDestroy()
    }
}
