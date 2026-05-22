package com.appointmed.mobile.auth

import android.content.Context
import com.appointmed.mobile.data.model.AuthResponse
import com.appointmed.mobile.data.model.RegisterRequest
import com.appointmed.mobile.data.network.ApiClient
import com.appointmed.mobile.util.NetworkUtils
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterPresenter(
    private var view: RegisterContract.View?,
    private val context: Context
) : RegisterContract.Presenter {

    private var selectedRole = "PATIENT"

    override fun onRoleSelected(role: String) {
        selectedRole = role
        view?.setRole(role)
    }

    private fun loadSpecializations() {
        ApiClient.create(context).getSpecializations().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    view?.showSpecializations(response.body() ?: emptyList())
                }
            }
            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                // Fail silently, use static list from arrays.xml
            }
        })
    }

    override fun onRegisterClicked(
        fullName: String, email: String, phone: String, address: String,
        gender: String, birthDate: String, specialization: String,
        licenseNumber: String, clinicAddress: String, consultationFee: String,
        password: String, confirmPassword: String, termsAccepted: Boolean
    ) {
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            view?.showError("Please fill in the required fields.")
            return
        }

        if (selectedRole == "PATIENT" && (gender.isEmpty() || birthDate.isEmpty())) {
            view?.showError("Patient registration requires gender and birth date.")
            return
        }

        if (selectedRole == "DOCTOR" && (specialization.isEmpty() || licenseNumber.isEmpty() || clinicAddress.isEmpty() || consultationFee.isEmpty())) {
            view?.showError("Doctor registration requires specialization, license, clinic address, and fee.")
            return
        }

        if (password != confirmPassword) {
            view?.showError("Passwords do not match.")
            return
        }

        if (!termsAccepted) {
            view?.showError("You must accept the Terms and Privacy Policy.")
            return
        }

        if (!NetworkUtils.isOnline(context)) {
            view?.showError("No internet connection. Please connect and try again.")
            return
        }

        view?.showLoading()

        val request = RegisterRequest(
            fullName = fullName,
            email = email,
            password = password,
            role = selectedRole,
            address = address.ifEmpty { null },
            gender = gender.ifEmpty { null },
            birthDate = birthDate.ifEmpty { null },
            specialization = specialization.ifEmpty { null },
            licenseNumber = licenseNumber.ifEmpty { null },
            phone = phone.ifEmpty { null },
            clinicAddress = clinicAddress.ifEmpty { null },
            consultationFee = consultationFee.toIntOrNull()
        )

        ApiClient.create(context).register(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.showMessage("Registration successful! Please sign in.")
                    view?.clearFormAndFinish()
                } else {
                    view?.showError(parseError(response.errorBody()))
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Unable to connect to the server. ${t.localizedMessage}")
            }
        })
    }

    override fun onDestroy() {
        view = null
    }

    private fun parseError(body: ResponseBody?): String {
        return try {
            val json = body?.string() ?: "Server returned an error."
            val message = JSONObject(json).optString("message")
            if (message.isNotEmpty()) message else "Unable to complete registration."
        } catch (exception: Exception) {
            "Unable to complete registration."
        }
    }
}
