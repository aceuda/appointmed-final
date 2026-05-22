package com.appointmed.mobile.specialist

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appointmed.mobile.R
import com.appointmed.mobile.booking.BookAppointmentActivity
import com.appointmed.mobile.dashboard.DashboardActivity
import com.appointmed.mobile.profile.ProfileActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class SelectSpecialistActivity : AppCompatActivity(), SelectSpecialistContract.View {

    private lateinit var presenter: SelectSpecialistContract.Presenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DoctorAdapter
    private lateinit var resultCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var chipsContainer: LinearLayout
    private var activeSpecialty = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_specialist)

        presenter = SelectSpecialistPresenter(this, this)

        recyclerView = findViewById(R.id.recyclerDoctors)
        resultCount = findViewById(R.id.textResultCount)
        progressBar = findViewById(R.id.progressBar)
        chipsContainer = findViewById(R.id.filterChipsContainer)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DoctorAdapter(emptyList()) { doctor -> presenter.onBookClicked(doctor) }
        recyclerView.adapter = adapter

        // Back button
        findViewById<ImageView>(R.id.btnBackSpecialist).setOnClickListener { finish() }

        // Search
        findViewById<EditText>(R.id.inputSearchDoctor).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { presenter.searchDoctors(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Bottom nav
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { presenter.onHomeClicked() }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { presenter.onProfileClicked() }
        findViewById<LinearLayout>(R.id.navSchedule).setOnClickListener { presenter.onScheduleClicked() }

        presenter.loadDoctors()
        presenter.loadSpecializations()
    }

    override fun showSpecializations(specs: List<String>) {
        chipsContainer.removeAllViews()
        val allSpecs = mutableListOf("All")
        allSpecs.addAll(specs)

        allSpecs.forEach { spec ->
            val button = Button(this).apply {
                text = spec
                textSize = 12f
                isAllCaps = false
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    36.dpToPx()
                )
                params.marginEnd = 8.dpToPx()
                layoutParams = params
                setPadding(16.dpToPx(), 0, 16.dpToPx(), 0)
                minWidth = 0
                minHeight = 0
                
                updateChipStyle(this, spec == activeSpecialty)
                
                setOnClickListener {
                    activeSpecialty = spec
                    presenter.filterBySpecialty(spec)
                    // Refresh all chips style
                    for (i in 0 until chipsContainer.childCount) {
                        val child = chipsContainer.getChildAt(i) as? Button ?: continue
                        updateChipStyle(child, child.text == activeSpecialty)
                    }
                }
            }
            chipsContainer.addView(button)
        }
    }

    private fun updateChipStyle(button: Button, isActive: Boolean) {
        if (isActive) {
            button.setBackgroundResource(R.drawable.bg_chip_active)
            button.setTextColor(resources.getColor(android.R.color.white, null))
        } else {
            button.setBackgroundResource(R.drawable.bg_chip_inactive)
            button.setTextColor(resources.getColor(R.color.textSecondary, null))
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    override fun showDoctors(doctors: List<DoctorItem>) {
        adapter.updateData(doctors)
    }

    override fun showFilteredDoctors(doctors: List<DoctorItem>, count: Int) {
        adapter.updateData(doctors)
        resultCount.text = "$count RESULTS"
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    override fun navigateToBooking(doctor: DoctorItem) {
        val intent = Intent(this, BookAppointmentActivity::class.java)
        intent.putExtra("doctor_id", doctor.id)
        intent.putExtra("doctor_name", doctor.name)
        intent.putExtra("doctor_specialty", doctor.specialty)
        intent.putExtra("doctor_fee", doctor.fee)
        intent.putExtra("doctor_clinic", doctor.clinic)
        startActivity(intent)
    }

    override fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun navigateToAppointments() {
        startActivity(Intent(this, com.appointmed.mobile.appointments.AppointmentsActivity::class.java))
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        if (::presenter.isInitialized) {
            presenter.onDestroy()
        }
        super.onDestroy()
    }

    // ---- RecyclerView Adapter ----
    inner class DoctorAdapter(
        private var doctors: List<DoctorItem>,
        private val onBookClick: (DoctorItem) -> Unit
    ) : RecyclerView.Adapter<DoctorAdapter.VH>() {

        fun updateData(newData: List<DoctorItem>) {
            doctors = newData
            notifyDataSetChanged()
        }

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val avatar: ImageView = view.findViewById(R.id.imageDoctorAvatar)
            val name: TextView = view.findViewById(R.id.textDoctorName)
            val specialty: TextView = view.findViewById(R.id.textDoctorSpecialty)
            val clinic: TextView = view.findViewById(R.id.textDoctorClinic)
            val fee: TextView = view.findViewById(R.id.textDoctorFee)
            val rating: TextView = view.findViewById(R.id.textDoctorRating)
            val btnBook: Button = view.findViewById(R.id.btnBookDoctor)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor_card, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val doc = doctors[position]
            holder.name.text = doc.name
            holder.specialty.text = doc.specialty
            holder.clinic.text = doc.clinic

            // Load avatar
            val avatarUrl = doc.avatarUrl
            val fullAvatarUrl = if (avatarUrl.isNullOrEmpty()) {
                null
            } else if (avatarUrl.startsWith("http")) {
                avatarUrl
            } else {
                com.appointmed.mobile.data.network.ApiClient.IMAGE_BASE_URL + avatarUrl
            }

            Glide.with(holder.itemView.context)
                .load(fullAvatarUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .transform(CenterCrop(), RoundedCorners(30))
                .into(holder.avatar)
            
            // Format fee with Peso sign
            holder.fee.text = if (doc.fee > 0) {
                "₱${String.format("%,.0f", doc.fee)}"
            } else {
                "₱0"
            }

            holder.rating.text = "★ ${doc.rating}"

            if (doc.available) {
                holder.btnBook.text = "Book"
                holder.btnBook.setBackgroundResource(R.drawable.bg_chip_active)
                holder.btnBook.setTextColor(holder.itemView.resources.getColor(android.R.color.white, null))
                holder.btnBook.isEnabled = true
            } else {
                holder.btnBook.text = "Unavailable"
                holder.btnBook.setBackgroundResource(R.drawable.bg_chip_inactive)
                holder.btnBook.setTextColor(holder.itemView.resources.getColor(R.color.textSecondary, null))
                holder.btnBook.isEnabled = false
            }
            holder.btnBook.setOnClickListener { onBookClick(doc) }
        }

        override fun getItemCount() = doctors.size
    }
}
