package com.appointmed.mobile.booking

import android.content.Context
import com.appointmed.mobile.data.local.Prefs
import com.appointmed.mobile.data.model.Appointment
import com.appointmed.mobile.data.model.AppointmentRequest
import com.appointmed.mobile.data.model.SlotStatus
import com.appointmed.mobile.data.network.ApiClient
import com.appointmed.mobile.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class BookAppointmentPresenter(
    private var view: BookAppointmentContract.View?,
    private val doctorId: Long,
    private val doctorName: String,
    private val doctorSpecialty: String,
    context: Context
) : BookAppointmentContract.Presenter {

    private val apiService: ApiService = ApiClient.create(context)
    private val prefs: Prefs = Prefs(context)
    private val calendar = Calendar.getInstance()
    private var selectedDay: Int? = null
    private var selectedSlot: String? = null

    private val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    override fun loadInitialData() {
        updateCalendar()
    }

    private fun updateCalendar() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1

        view?.showMonthLabel("${monthNames[month]} $year")
        view?.showCalendar(year, month, daysInMonth, firstDayOfWeek)

        // If a day was selected, reload slots for it
        selectedDay?.let { fetchSlotsForDate(it) }
    }

    override fun onDateSelected(day: Int) {
        selectedDay = day
        selectedSlot = null
        view?.highlightSelectedDate(day)
        fetchSlotsForDate(day)
    }

    private fun fetchSlotsForDate(day: Int) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val dateStr = String.format("%d-%02d-%02d", year, month, day)

        view?.showLoading()
        apiService.getSlotsWithStatus(doctorId, dateStr).enqueue(object : Callback<List<SlotStatus>> {
            override fun onResponse(call: Call<List<SlotStatus>>, response: Response<List<SlotStatus>>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    val slots = response.body() ?: emptyList()
                    val slotInfos = slots.map { slot ->
                        // Determine if past time (for today)
                        val now = Calendar.getInstance()
                        val isToday = year == now.get(Calendar.YEAR) &&
                            calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                            day == now.get(Calendar.DAY_OF_MONTH)

                        var status = slot.status
                        if (isToday && status == "available") {
                            try {
                                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val slotTime = sdf.parse(slot.time)
                                val nowTime = sdf.parse(sdf.format(now.time))
                                if (slotTime != null && nowTime != null && slotTime.before(nowTime)) {
                                    status = "past"
                                }
                            } catch (_: Exception) {}
                        }

                        SlotInfo(time = slot.time, status = status)
                    }
                    view?.showTimeSlots(slotInfos)
                } else {
                    view?.showError("Failed to load time slots")
                    view?.showTimeSlots(emptyList())
                }
            }

            override fun onFailure(call: Call<List<SlotStatus>>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
                view?.showTimeSlots(emptyList())
            }
        })
    }

    override fun onSlotSelected(slot: String) {
        selectedSlot = slot
        view?.highlightSelectedSlot(slot)
    }

    override fun onPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
        selectedDay = null
        selectedSlot = null
        updateCalendar()
        view?.showTimeSlots(emptyList()) // Clear slots
    }

    override fun onNextMonth() {
        calendar.add(Calendar.MONTH, 1)
        selectedDay = null
        selectedSlot = null
        updateCalendar()
        view?.showTimeSlots(emptyList()) // Clear slots
    }

    override fun onContinueClicked(reason: String) {
        if (selectedDay == null) {
            view?.showError("Please select a date.")
            return
        }
        if (selectedSlot == null) {
            view?.showError("Please select a time slot.")
            return
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val dateStr = String.format("%d-%02d-%02d", year, month, selectedDay!!)

        val patientId = prefs.getUser().id
        if (patientId == 0L) {
            view?.showError("Session expired. Please login again.")
            return
        }

        val request = AppointmentRequest(
            patientId = patientId,
            doctorId = doctorId,
            appointmentDate = dateStr,
            appointmentTime = selectedSlot!!,
            reason = reason.ifBlank { null }
        )

        view?.showBookingProgress(true)
        apiService.createAppointment(request).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                view?.showBookingProgress(false)
                if (response.isSuccessful) {
                    val displayDate = "${monthNames[calendar.get(Calendar.MONTH)]} $selectedDay, $year"
                    view?.navigateToConfirmation(doctorName, doctorSpecialty, displayDate, selectedSlot!!)
                } else if (response.code() == 409) {
                    view?.showError("This slot is no longer available. Please select another.")
                    // Refresh slots
                    selectedDay?.let { fetchSlotsForDate(it) }
                } else {
                    view?.showError("Booking failed. Please try again.")
                }
            }

            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                view?.showBookingProgress(false)
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun onCancelClicked() {
        view?.navigateBack()
    }

    override fun onDestroy() {
        view = null
    }
}
