package com.appointmed.mobile.auth

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.appointmed.mobile.R

class RegisterActivity : AppCompatActivity(), RegisterContract.View {
    private lateinit var imageRegisterBack: ImageView
    private lateinit var buttonRolePatient: Button
    private lateinit var buttonRoleDoctor: Button
    private lateinit var buttonRegister: Button
    private lateinit var buttonGoLogin: Button
    private lateinit var inputFullName: EditText
    private lateinit var inputEmailRegister: EditText
    private lateinit var inputPhone: EditText
    private lateinit var inputAddress: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var inputBirthDate: EditText
    private lateinit var spinnerSpecialization: Spinner
    private lateinit var inputLicenseNumber: EditText
    private lateinit var inputClinicAddress: EditText
    private lateinit var inputConsultationFee: EditText
    private lateinit var inputPasswordRegister: EditText
    private lateinit var inputConfirmPassword: EditText
    private lateinit var checkboxTerms: CheckBox
    private lateinit var patientFieldsContainer: LinearLayout
    private lateinit var doctorFieldsContainer: LinearLayout
    private lateinit var progressBarRegister: ProgressBar
    private lateinit var textRegisterError: TextView
    private lateinit var textRegisterMessage: TextView

    private lateinit var presenter: RegisterContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        presenter = RegisterPresenter(this, this)

        imageRegisterBack = findViewById(R.id.imageRegisterBack)
        buttonRolePatient = findViewById(R.id.buttonRolePatient)
        buttonRoleDoctor = findViewById(R.id.buttonRoleDoctor)
        buttonRegister = findViewById(R.id.buttonRegister)
        buttonGoLogin = findViewById(R.id.buttonGoLogin)
        inputFullName = findViewById(R.id.inputFullName)
        inputEmailRegister = findViewById(R.id.inputEmailRegister)
        inputPhone = findViewById(R.id.inputPhone)
        inputAddress = findViewById(R.id.inputAddress)
        spinnerGender = findViewById<Spinner>(R.id.spinnerGender)
        inputBirthDate = findViewById(R.id.inputBirthDate)
        spinnerSpecialization = findViewById(R.id.spinnerSpecialization)
        inputLicenseNumber = findViewById(R.id.inputLicenseNumber)
        inputClinicAddress = findViewById(R.id.inputClinicAddress)
        inputConsultationFee = findViewById(R.id.inputConsultationFee)
        inputPasswordRegister = findViewById(R.id.inputPasswordRegister)
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword)
        checkboxTerms = findViewById(R.id.checkboxTerms)
        patientFieldsContainer = findViewById(R.id.patientFieldsContainer)
        doctorFieldsContainer = findViewById(R.id.doctorFieldsContainer)
        progressBarRegister = findViewById(R.id.progressBarRegister)
        textRegisterError = findViewById(R.id.textRegisterError)
        textRegisterMessage = findViewById(R.id.textRegisterMessage)

        buttonRolePatient.setOnClickListener { presenter.onRoleSelected("PATIENT") }
        buttonRoleDoctor.setOnClickListener { presenter.onRoleSelected("DOCTOR") }
        buttonRegister.setOnClickListener {
            presenter.onRegisterClicked(
                fullName = inputFullName.text.toString().trim(),
                email = inputEmailRegister.text.toString().trim(),
                phone = inputPhone.text.toString().trim(),
                address = inputAddress.text.toString().trim(),
                gender = spinnerGender.selectedItem?.toString()?.takeIf { it != "Select" } ?: "",
                birthDate = inputBirthDate.text.toString().trim(),
                specialization = spinnerSpecialization.selectedItem?.toString()?.takeIf { it != "Select Specialization" } ?: "",
                licenseNumber = inputLicenseNumber.text.toString().trim(),
                clinicAddress = inputClinicAddress.text.toString().trim(),
                consultationFee = inputConsultationFee.text.toString().trim(),
                password = inputPasswordRegister.text.toString().trim(),
                confirmPassword = inputConfirmPassword.text.toString().trim(),
                termsAccepted = checkboxTerms.isChecked
            )
        }
        buttonGoLogin.setOnClickListener { finish() }
        imageRegisterBack.setOnClickListener { finish() }

        presenter.onRoleSelected("PATIENT")
    }

    override fun setRole(role: String) {
        val tabs = listOf(
            buttonRolePatient to "PATIENT",
            buttonRoleDoctor to "DOCTOR"
        )

        for ((button, tabRole) in tabs) {
            if (role == tabRole) {
                button.setBackgroundResource(R.drawable.bg_tab_active)
                button.setTextColor(getColor(R.color.textPrimary))
                button.elevation = 2f
            } else {
                button.setBackgroundResource(R.drawable.bg_tab_inactive)
                button.setTextColor(getColor(R.color.textSecondary))
                button.elevation = 0f
            }
        }

        patientFieldsContainer.visibility = if (role == "PATIENT") View.VISIBLE else View.GONE
        doctorFieldsContainer.visibility = if (role == "DOCTOR") View.VISIBLE else View.GONE
    }

    override fun showSpecializations(specs: List<String>) {
        if (specs.isEmpty()) return
        
        val allSpecs = mutableListOf("Select Specialization")
        allSpecs.addAll(specs)
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allSpecs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialization.adapter = adapter
    }

    override fun showLoading() {
        progressBarRegister.visibility = View.VISIBLE
        buttonRegister.isEnabled = false
        buttonGoLogin.isEnabled = false
        buttonRolePatient.isEnabled = false
        buttonRoleDoctor.isEnabled = false
    }

    override fun hideLoading() {
        progressBarRegister.visibility = View.GONE
        buttonRegister.isEnabled = true
        buttonGoLogin.isEnabled = true
        buttonRolePatient.isEnabled = true
        buttonRoleDoctor.isEnabled = true
    }

    override fun showError(message: String) {
        textRegisterError.text = message
        textRegisterError.visibility = View.VISIBLE
        textRegisterMessage.visibility = View.GONE
    }

    override fun showMessage(message: String) {
        textRegisterMessage.text = message
        textRegisterMessage.visibility = View.VISIBLE
        textRegisterError.visibility = View.GONE
    }

    override fun clearFormAndFinish() {
        inputFullName.setText("")
        inputEmailRegister.setText("")
        inputPasswordRegister.setText("")
        inputConfirmPassword.setText("")
        checkboxTerms.isChecked = false
        buttonGoLogin.postDelayed({ finish() }, 1500)
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}
