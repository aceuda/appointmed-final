package com.appointmed.mobile.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.appointmed.mobile.R
import com.appointmed.mobile.auth.LoginActivity
import com.appointmed.mobile.data.model.User

class ProfileActivity : AppCompatActivity(), ProfileContract.View {
    private lateinit var imageProfileBack: ImageView
    private lateinit var imageProfileAvatar: ImageView
    private lateinit var avatarSelector: FrameLayout
    private lateinit var inputProfileName: EditText
    private lateinit var inputProfileEmail: EditText
    private lateinit var textProfilePatientId: TextView
    private lateinit var inputPhone: EditText
    private lateinit var inputAddress: EditText
    private lateinit var inputBirthDate: EditText
    private lateinit var spinnerBloodType: Spinner
    private lateinit var inputProfileFee: EditText
    private lateinit var layoutProfileFee: View
    private lateinit var layoutProfileDOBAndBlood: View
    private lateinit var buttonSaveChanges: Button
    private lateinit var buttonDiscardChanges: Button
    private lateinit var buttonLogout: Button

    // Change password fields
    private lateinit var layoutPasswordHeader: View
    private lateinit var layoutPasswordContent: View
    private lateinit var imagePasswordArrow: ImageView
    private lateinit var inputCurrentPassword: EditText
    private lateinit var inputNewPassword: EditText
    private lateinit var inputConfirmNewPassword: EditText
    private lateinit var buttonUpdatePassword: Button
    private lateinit var textPasswordError: TextView
    private lateinit var textPasswordSuccess: TextView
    private lateinit var progressBarPassword: ProgressBar

    private var selectedAvatarData: String? = null
    private lateinit var presenter: ProfileContract.Presenter

    private val avatarPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processSelectedAvatar(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        presenter = ProfilePresenter(this, this)

        imageProfileBack = findViewById(R.id.imageProfileBack)
        imageProfileAvatar = findViewById(R.id.imageProfileAvatar)
        avatarSelector = findViewById(R.id.avatarSelector)
        inputProfileName = findViewById(R.id.inputProfileName)
        inputProfileEmail = findViewById(R.id.inputProfileEmail)
        textProfilePatientId = findViewById(R.id.textProfilePatientId)
        inputPhone = findViewById(R.id.inputPhone)
        inputAddress = findViewById(R.id.inputAddress)
        inputBirthDate = findViewById(R.id.inputBirthDate)
        spinnerBloodType = findViewById(R.id.spinnerBloodType)
        inputProfileFee = findViewById(R.id.inputProfileFee)
        layoutProfileFee = findViewById(R.id.layoutProfileFee)
        layoutProfileDOBAndBlood = findViewById(R.id.layoutProfileDOBAndBlood)
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges)
        buttonDiscardChanges = findViewById(R.id.buttonDiscardChanges)
        buttonLogout = findViewById(R.id.buttonLogout)

        // Change password bindings
        layoutPasswordHeader = findViewById(R.id.layoutPasswordHeader)
        layoutPasswordContent = findViewById(R.id.layoutPasswordContent)
        imagePasswordArrow = findViewById(R.id.imagePasswordArrow)
        inputCurrentPassword = findViewById(R.id.inputCurrentPassword)
        inputNewPassword = findViewById(R.id.inputNewPassword)
        inputConfirmNewPassword = findViewById(R.id.inputConfirmNewPassword)
        buttonUpdatePassword = findViewById(R.id.buttonUpdatePassword)
        textPasswordError = findViewById(R.id.textPasswordError)
        textPasswordSuccess = findViewById(R.id.textPasswordSuccess)
        progressBarPassword = findViewById(R.id.progressBarPassword)

        presenter.loadProfile()

        avatarSelector.setOnClickListener {
            avatarPicker.launch("image/*")
        }

        buttonSaveChanges.setOnClickListener {
            presenter.saveProfile(
                name = inputProfileName.text.toString().trim(),
                email = inputProfileEmail.text.toString().trim(),
                phone = inputPhone.text.toString().trim(),
                address = inputAddress.text.toString().trim(),
                birthDate = inputBirthDate.text.toString().trim(),
                bloodType = spinnerBloodType.selectedItem.toString().takeIf { it != "Select" } ?: "",
                consultationFee = inputProfileFee.text.toString().trim(),
                avatarData = selectedAvatarData
            )
        }

        buttonDiscardChanges.setOnClickListener {
            presenter.loadProfile()
            Toast.makeText(this, "Changes discarded.", Toast.LENGTH_SHORT).show()
        }

        imageProfileBack.setOnClickListener { finish() }

        buttonLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        findViewById<View>(R.id.navHome).setOnClickListener { presenter.onHomeClicked() }
        findViewById<View>(R.id.navSchedule).setOnClickListener { presenter.onScheduleClicked() }
        findViewById<View>(R.id.navRecords).setOnClickListener { presenter.onRecordsClicked() }

        layoutPasswordHeader.setOnClickListener {
            togglePasswordSection()
        }

        buttonUpdatePassword.setOnClickListener {
            presenter.changePassword(
                currentPassword = inputCurrentPassword.text.toString().trim(),
                newPassword = inputNewPassword.text.toString().trim(),
                confirmPassword = inputConfirmNewPassword.text.toString().trim()
            )
        }
    }

    override fun populateFields(user: User, phone: String?, address: String?, birthDate: String?, bloodType: String?, consultationFee: Double) {
        inputProfileName.setText(user.name)
        inputProfileEmail.setText(user.email)
        
        if (user.role == "DOCTOR") {
            textProfilePatientId.text = "Doctor ID: #${user.id}"
            layoutProfileFee.visibility = View.VISIBLE
            layoutProfileDOBAndBlood.visibility = View.GONE
            inputProfileFee.setText(consultationFee.toString())
        } else {
            textProfilePatientId.text = getString(R.string.profile_patient_id, user.id)
            layoutProfileFee.visibility = View.GONE
            layoutProfileDOBAndBlood.visibility = View.VISIBLE
        }

        inputPhone.setText(phone ?: "")
        inputAddress.setText(address ?: "")
        inputBirthDate.setText(birthDate ?: "")

        val savedBloodType = bloodType ?: ""
        val bloodTypes = resources.getStringArray(R.array.blood_type_options)
        val selectedIndex = bloodTypes.indexOf(savedBloodType).takeIf { it >= 0 } ?: 0
        spinnerBloodType.setSelection(selectedIndex)
    }

    override fun setAvatarFromBase64(data: String) {
        try {
            val avatarBytes = Base64.decode(data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.size)
            imageProfileAvatar.setImageBitmap(bitmap)
        } catch (exception: Exception) {
            setDefaultAvatar()
        }
    }

    override fun setDefaultAvatar() {
        imageProfileAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
    }

    override fun showProfileLoading(isLoading: Boolean) {
        buttonSaveChanges.isEnabled = !isLoading
        buttonDiscardChanges.isEnabled = !isLoading
    }

    override fun showProfileUpdateSuccess() {
        Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
    }

    override fun showProfileUpdateError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showPasswordLoading(isLoading: Boolean) {
        progressBarPassword.visibility = if (isLoading) View.VISIBLE else View.GONE
        buttonUpdatePassword.isEnabled = !isLoading
    }

    override fun showPasswordError(message: String) {
        textPasswordError.text = message
        textPasswordError.visibility = View.VISIBLE
        textPasswordSuccess.visibility = View.GONE
    }

    override fun showPasswordSuccess(message: String) {
        textPasswordSuccess.text = message
        textPasswordSuccess.visibility = View.VISIBLE
        textPasswordError.visibility = View.GONE
    }

    override fun clearPasswordFields() {
        inputCurrentPassword.setText("")
        inputNewPassword.setText("")
        inputConfirmNewPassword.setText("")
    }

    override fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                presenter.onLogoutConfirmed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun navigateToHome() {
        startActivity(Intent(this, com.appointmed.mobile.dashboard.DashboardActivity::class.java))
        finish()
    }

    override fun navigateToAppointments() {
        startActivity(Intent(this, com.appointmed.mobile.appointments.AppointmentsActivity::class.java))
    }

    override fun navigateToRecords() {
        Toast.makeText(this, "Health Records feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun togglePasswordSection() {
        if (layoutPasswordContent.visibility == View.VISIBLE) {
            layoutPasswordContent.visibility = View.GONE
            imagePasswordArrow.rotation = 0f
        } else {
            layoutPasswordContent.visibility = View.VISIBLE
            imagePasswordArrow.rotation = 180f
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.loadProfile()
    }

    private fun processSelectedAvatar(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                selectedAvatarData = Base64.encodeToString(bytes, Base64.DEFAULT)
                imageProfileAvatar.setImageURI(uri)
            }
        } catch (exception: Exception) {
            Toast.makeText(this, "Unable to load selected image.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}
