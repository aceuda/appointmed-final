package com.appointmed.mobile.booking

interface BookAppointmentContract {
    interface View {
        fun showCalendar(year: Int, month: Int, daysInMonth: Int, firstDayOfWeek: Int)
        fun highlightSelectedDate(day: Int)
        fun showTimeSlots(slots: List<SlotInfo>)
        fun highlightSelectedSlot(slot: String)
        fun showMonthLabel(label: String)
        fun navigateToConfirmation(doctorName: String, specialty: String, date: String, time: String)
        fun navigateBack()
        fun showError(message: String)
        fun showLoading()
        fun hideLoading()
        fun showBookingProgress(show: Boolean)
    }

    interface Presenter {
        fun loadInitialData()
        fun onDateSelected(day: Int)
        fun onSlotSelected(slot: String)
        fun onPreviousMonth()
        fun onNextMonth()
        fun onContinueClicked(reason: String)
        fun onCancelClicked()
        fun onDestroy()
    }
}

/** Represents a time slot with its availability status */
data class SlotInfo(
    val time: String,
    val status: String // available, booked, blocked, past
)
