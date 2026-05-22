package com.appointmed.mobile.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appointmed.mobile.R
import com.appointmed.mobile.data.model.Appointment
import com.appointmed.mobile.data.network.ApiClient
import com.bumptech.glide.Glide

class DoctorDashboardAppointmentAdapter(
    private var appointments: List<Appointment>,
    private val onConfirmClick: (Long) -> Unit,
    private val onCompleteClick: (Long) -> Unit
) : RecyclerView.Adapter<DoctorDashboardAppointmentAdapter.ViewHolder>() {

    fun updateData(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageAvatar: ImageView = view.findViewById(R.id.imagePatientAvatar)
        val textName: TextView = view.findViewById(R.id.textPatientName)
        val textDetails: TextView = view.findViewById(R.id.textAppointmentDetails)
        val textBadge: TextView = view.findViewById(R.id.textStatusBadge)
        val buttonAction: Button = view.findViewById(R.id.buttonAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_dashboard_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appt = appointments[position]
        holder.textName.text = appt.patient?.name ?: "Patient"
        holder.textDetails.text = "${appt.reason ?: "Consultation"} • ${appt.appointmentTime ?: ""}"
        
        // Avatar
        val avatarData = appt.patient?.avatarData
        if (!avatarData.isNullOrEmpty()) {
            val fullUrl = if (avatarData.startsWith("http")) avatarData else ApiClient.IMAGE_BASE_URL + avatarData
            if (avatarData.startsWith("data:image")) {
                try {
                    val cleanData = avatarData.substringAfter(",")
                    val decodedBytes = android.util.Base64.decode(cleanData, android.util.Base64.NO_WRAP)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    Glide.with(holder.itemView.context).load(bitmap).circleCrop().into(holder.imageAvatar)
                    holder.imageAvatar.imageTintList = null
                } catch (e: Exception) {
                    holder.imageAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
                    holder.imageAvatar.imageTintList = ColorStateList.valueOf(Color.parseColor("#CBD5E1"))
                }
            } else {
                Glide.with(holder.itemView.context).load(fullUrl).circleCrop().into(holder.imageAvatar)
                holder.imageAvatar.imageTintList = null
            }
        } else {
            holder.imageAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
            holder.imageAvatar.imageTintList = ColorStateList.valueOf(Color.parseColor("#CBD5E1"))
        }

        // Status badge
        holder.textBadge.text = appt.status
        when (appt.status) {
            "CONFIRMED" -> {
                holder.textBadge.setBackgroundColor(Color.parseColor("#10B981"))
                holder.buttonAction.visibility = View.VISIBLE
                holder.buttonAction.text = "Complete"
                holder.buttonAction.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3B82F6"))
                holder.buttonAction.setOnClickListener { onCompleteClick(appt.id) }
            }
            "COMPLETED" -> {
                holder.textBadge.setBackgroundColor(Color.parseColor("#3B82F6"))
                holder.buttonAction.visibility = View.GONE
            }
            "CANCELLED" -> {
                holder.textBadge.setBackgroundColor(Color.parseColor("#EF4444"))
                holder.buttonAction.visibility = View.GONE
            }
            "PENDING" -> {
                holder.textBadge.setBackgroundColor(Color.parseColor("#F59E0B"))
                holder.buttonAction.visibility = View.VISIBLE
                holder.buttonAction.text = "Confirm"
                holder.buttonAction.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
                holder.buttonAction.setOnClickListener { onConfirmClick(appt.id) }
            }
            else -> {
                holder.textBadge.setBackgroundColor(Color.parseColor("#64748B"))
                holder.buttonAction.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = appointments.size
}
