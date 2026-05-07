package com.appointmed.mobile.booking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.appointmed.mobile.R
import com.google.android.flexbox.FlexboxLayout

class BookAppointmentActivity : AppCompatActivity(), BookAppointmentContract.View {

    private lateinit var presenter: BookAppointmentContract.Presenter
    private lateinit var calendarGrid: GridLayout
    private lateinit var timeSlotsContainer: FlexboxLayout
    private lateinit var monthLabel: TextView
    private lateinit var inputReason: EditText
    private lateinit var btnContinue: Button
    private lateinit var progressBar: ProgressBar

    private var doctorId = 0L
    private var doctorName = ""
    private var doctorSpecialty = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        doctorId = intent.getLongExtra("doctor_id", 0L)
        doctorName = intent.getStringExtra("doctor_name") ?: "Doctor"
        doctorSpecialty = intent.getStringExtra("doctor_specialty") ?: ""

        presenter = BookAppointmentPresenter(this, doctorId, doctorName, doctorSpecialty, this)

        calendarGrid = findViewById(R.id.calendarGrid)
        monthLabel = findViewById(R.id.textCurrentMonth)
        inputReason = findViewById(R.id.inputReason)
        timeSlotsContainer = findViewById(R.id.timeSlotsContainer)
        btnContinue = findViewById(R.id.btnContinuePayment)
        progressBar = findViewById(R.id.progressBarBooking)

        // Navigation
        findViewById<ImageView>(R.id.btnBackBooking).setOnClickListener { presenter.onCancelClicked() }
        findViewById<Button>(R.id.btnCancelBooking).setOnClickListener { presenter.onCancelClicked() }
        btnContinue.setOnClickListener {
            presenter.onContinueClicked(inputReason.text.toString())
        }

        // Calendar navigation
        findViewById<ImageView>(R.id.btnPrevMonth).setOnClickListener { presenter.onPreviousMonth() }
        findViewById<ImageView>(R.id.btnNextMonth).setOnClickListener { presenter.onNextMonth() }

        presenter.loadInitialData()
    }

    override fun showMonthLabel(label: String) {
        monthLabel.text = label
    }

    override fun showCalendar(year: Int, month: Int, daysInMonth: Int, firstDayOfWeek: Int) {
        calendarGrid.removeAllViews()
        val cellSize = resources.displayMetrics.widthPixels / 7 - 24

        // Empty cells before first day
        for (i in 0 until firstDayOfWeek) {
            val empty = TextView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSize; height = cellSize
                }
            }
            calendarGrid.addView(empty)
        }

        // Day cells
        for (day in 1..daysInMonth) {
            val tv = TextView(this).apply {
                text = "$day"
                textSize = 14f
                setTextColor(Color.parseColor("#334155"))
                gravity = Gravity.CENTER
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSize; height = cellSize
                }
                setOnClickListener { presenter.onDateSelected(day) }
            }
            calendarGrid.addView(tv)
        }
    }

    override fun highlightSelectedDate(day: Int) {
        for (i in 0 until calendarGrid.childCount) {
            val child = calendarGrid.getChildAt(i) as? TextView ?: continue
            if (child.text.toString() == "$day") {
                child.setBackgroundResource(R.drawable.bg_cal_selected)
                child.setTextColor(Color.WHITE)
            } else {
                child.background = null
                child.setTextColor(Color.parseColor("#334155"))
            }
        }
    }

    override fun showTimeSlots(slots: List<SlotInfo>) {
        timeSlotsContainer.removeAllViews()

        if (slots.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "Select a date to see available slots"
                textSize = 13f
                setTextColor(Color.parseColor("#94a3b8"))
                gravity = Gravity.CENTER
                val params = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.MATCH_PARENT,
                    48.dpToPx()
                )
                layoutParams = params
            }
            timeSlotsContainer.addView(emptyText)
            return
        }

        slots.forEach { slotInfo ->
            val btn = Button(this).apply {
                text = slotInfo.time
                textSize = 12f
                isAllCaps = false
                setTextColor(Color.parseColor("#334155")) // Ensure text is visible
                
                val params = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    44.dpToPx()
                ).apply {
                    marginEnd = 8.dpToPx()
                    bottomMargin = 8.dpToPx()
                }
                layoutParams = params
                setPadding(16.dpToPx(), 0, 16.dpToPx(), 0)
                minWidth = 0
                minHeight = 0
                
                when (slotInfo.status) {
                    "available" -> {
                        setBackgroundResource(R.drawable.bg_slot_unselected)
                        isEnabled = true
                        setOnClickListener { presenter.onSlotSelected(slotInfo.time) }
                    }
                    "booked" -> {
                        setBackgroundResource(R.drawable.bg_slot_unselected)
                        setTextColor(Color.parseColor("#cbd5e1"))
                        isEnabled = false
                        alpha = 0.6f
                        text = "${slotInfo.time} ● Booked"
                    }
                    "blocked" -> {
                        setBackgroundResource(R.drawable.bg_slot_unselected)
                        setTextColor(Color.parseColor("#f59e0b"))
                        isEnabled = false
                        alpha = 0.7f
                        text = "${slotInfo.time} ● Unavailable"
                    }
                    "past" -> {
                        setBackgroundResource(R.drawable.bg_slot_unselected)
                        setTextColor(Color.parseColor("#cbd5e1"))
                        isEnabled = false
                        alpha = 0.4f
                        text = "${slotInfo.time} ● Past"
                    }
                }
                tag = slotInfo.time
            }
            timeSlotsContainer.addView(btn)
        }
    }

    override fun highlightSelectedSlot(slot: String) {
        for (i in 0 until timeSlotsContainer.childCount) {
            val btn = timeSlotsContainer.getChildAt(i) as? Button ?: continue
            if (btn.tag == slot) {
                btn.setBackgroundResource(R.drawable.bg_slot_selected)
                btn.setTextColor(Color.WHITE)
            } else if (btn.isEnabled) {
                btn.setBackgroundResource(R.drawable.bg_slot_unselected)
                btn.setTextColor(Color.parseColor("#334155"))
            }
        }
    }

    override fun navigateToConfirmation(doctorName: String, specialty: String, date: String, time: String) {
        val intent = Intent(this, BookingConfirmedActivity::class.java).apply {
            putExtra("doctor_name", doctorName)
            putExtra("doctor_specialty", specialty)
            putExtra("date", date)
            putExtra("time", time)
        }
        startActivity(intent)
        finish()
    }

    override fun navigateBack() {
        finish()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    override fun showBookingProgress(show: Boolean) {
        btnContinue.isEnabled = !show
        btnContinue.text = if (show) "Booking…" else "Confirm Booking"
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
