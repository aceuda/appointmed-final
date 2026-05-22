package com.appointmed.mobile.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appointmed.mobile.R
import com.appointmed.mobile.data.model.SlotStatus
import java.text.SimpleDateFormat
import java.util.*

class DoctorDashboardSlotAdapter(
    private var slots: List<SlotStatus>,
    private val onSlotClick: (SlotStatus) -> Unit
) : RecyclerView.Adapter<DoctorDashboardSlotAdapter.ViewHolder>() {

    fun updateData(newSlots: List<SlotStatus>) {
        slots = newSlots
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.layoutSlotContainer)
        val textTime: TextView = view.findViewById(R.id.textSlotTime)
        val textStatus: TextView = view.findViewById(R.id.textSlotStatus)
        val textPatientName: TextView = view.findViewById(R.id.textSlotPatientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_dashboard_slot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = slots[position]
        
        // Format time 08:00 to 08:00 AM
        try {
            val parser = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = parser.parse(slot.time)
            holder.textTime.text = formatter.format(date!!)
        } catch (e: Exception) {
            holder.textTime.text = slot.time
        }

        // Check if slot is past today
        var isPast = false
        try {
            val now = Calendar.getInstance()
            val slotTimeParts = slot.time.split(":")
            val slotCal = Calendar.getInstance()
            slotCal.set(Calendar.HOUR_OF_DAY, slotTimeParts[0].toInt())
            slotCal.set(Calendar.MINUTE, slotTimeParts[1].toInt())
            slotCal.set(Calendar.SECOND, 0)
            slotCal.set(Calendar.MILLISECOND, 0)
            
            // A slot is past if the current time is at or after its start time.
            if (now.timeInMillis >= slotCal.timeInMillis) {
                isPast = true
            }
        } catch (e: Exception) {}

        holder.textPatientName.visibility = View.GONE
        
        val isBooked = slot.status == "booked"
        val isBlocked = slot.status == "blocked"

        if (isPast && !isBooked) {
            // Past available/blocked slots are just grayed out
            holder.container.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1F5F9"))
            holder.textTime.setTextColor(Color.parseColor("#94A3B8"))
            holder.textStatus.setTextColor(Color.parseColor("#94A3B8"))
            holder.textStatus.text = "Past"
            holder.itemView.setOnClickListener(null)
        } else {
            when (slot.status) {
                "available" -> {
                    holder.container.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F0FDFA"))
                    holder.textTime.setTextColor(Color.parseColor("#0F766E"))
                    holder.textStatus.setTextColor(Color.parseColor("#14B8A6"))
                    holder.textStatus.text = "Available"
                    holder.itemView.setOnClickListener { onSlotClick(slot) }
                }
                "booked" -> {
                    holder.container.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
                    holder.textTime.setTextColor(Color.parseColor("#1D4ED8"))
                    holder.textStatus.setTextColor(Color.parseColor("#3B82F6"))
                    holder.textStatus.text = "Booked"
                    if (!slot.patientName.isNullOrEmpty()) {
                        holder.textPatientName.visibility = View.VISIBLE
                        holder.textPatientName.text = slot.patientName
                    }
                    holder.itemView.setOnClickListener(null)
                }
                "blocked" -> {
                    holder.container.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEF2F2"))
                    holder.textTime.setTextColor(Color.parseColor("#B91C1C"))
                    holder.textStatus.setTextColor(Color.parseColor("#EF4444"))
                    holder.textStatus.text = "Blocked"
                    holder.itemView.setOnClickListener { onSlotClick(slot) }
                }
            }
        }
    }

    override fun getItemCount() = slots.size
}
