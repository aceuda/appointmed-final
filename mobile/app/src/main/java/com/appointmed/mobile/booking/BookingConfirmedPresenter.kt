package com.appointmed.mobile.booking

class BookingConfirmedPresenter(
    private var view: BookingConfirmedContract.View?,
    private val doctorName: String,
    private val doctorSpecialty: String,
    private val date: String,
    private val time: String
) : BookingConfirmedContract.Presenter {

    override fun loadDetails() {
        view?.showConfirmationDetails(doctorName, doctorSpecialty, date, time)
    }

    override fun onDashboardClicked() {
        view?.navigateToDashboard()
    }

    override fun onAppointmentsClicked() {
        view?.navigateToAppointments()
    }

    override fun onDestroy() {
        view = null
    }
}
