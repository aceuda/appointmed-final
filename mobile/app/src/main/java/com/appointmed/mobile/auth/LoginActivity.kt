package com.appointmed.mobile.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.appointmed.mobile.R
import com.appointmed.mobile.dashboard.DashboardActivity
import com.appointmed.mobile.data.model.User

class LoginActivity : AppCompatActivity(), LoginContract.View {
    private lateinit var buttonPatient: Button
    private lateinit var buttonDoctor: Button
    private lateinit var buttonLogin: Button
    private lateinit var buttonGoRegister: TextView
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var textError: TextView
    private lateinit var textMessage: TextView
    private lateinit var buttonTogglePassword: ImageView
    private var isPasswordVisible = false

    private lateinit var presenter: LoginContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        presenter = LoginPresenter(this, this)

        buttonPatient = findViewById(R.id.buttonPatient)
        buttonDoctor = findViewById(R.id.buttonDoctor)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonGoRegister = findViewById(R.id.buttonGoRegister)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        progressBar = findViewById(R.id.progressBar)
        textError = findViewById(R.id.textError)
        textMessage = findViewById(R.id.textMessage)
        buttonTogglePassword = findViewById(R.id.buttonTogglePassword)

        buttonPatient.setOnClickListener { presenter.onRoleSelected("PATIENT") }
        buttonDoctor.setOnClickListener { presenter.onRoleSelected("DOCTOR") }
        buttonLogin.setOnClickListener {
            presenter.onLoginClicked(
                inputEmail.text.toString().trim(),
                inputPassword.text.toString().trim()
            )
        }
        buttonGoRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }

        findViewById<View>(R.id.buttonBack).setOnClickListener { finish() }
        findViewById<View>(R.id.buttonForgot).setOnClickListener {
            showMessage("Forgot password? Feature coming soon!")
        }

        buttonTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                inputPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                buttonTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel) // Example placeholder
            } else {
                inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                buttonTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }
            inputPassword.setSelection(inputPassword.text.length)
        }

        presenter.onRoleSelected("PATIENT")
    }

    override fun setRole(role: String) {
        val tabs = listOf(
            buttonPatient to "PATIENT",
            buttonDoctor to "DOCTOR"
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
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
        buttonLogin.isEnabled = false
        buttonGoRegister.isEnabled = false
        buttonPatient.isEnabled = false
        buttonDoctor.isEnabled = false
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
        buttonLogin.isEnabled = true
        buttonGoRegister.isEnabled = true
        buttonPatient.isEnabled = true
        buttonDoctor.isEnabled = true
    }

    override fun showError(message: String) {
        textError.text = message
        textError.visibility = View.VISIBLE
        textMessage.visibility = View.GONE
    }

    override fun showMessage(message: String) {
        textMessage.text = message
        textMessage.visibility = View.VISIBLE
        textError.visibility = View.GONE
    }

    override fun navigateToDashboard(user: User) {
        val intent = if (user.role == "ADMIN") {
            Intent(this, com.appointmed.mobile.admin.AdminDashboardActivity::class.java)
        } else {
            Intent(this, DashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}
