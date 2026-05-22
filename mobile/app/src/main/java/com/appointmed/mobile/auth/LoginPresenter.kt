package com.appointmed.mobile.auth

import android.content.Context
import com.appointmed.mobile.data.model.AuthResponse
import com.appointmed.mobile.data.model.LoginRequest
import com.appointmed.mobile.data.local.Prefs
import com.appointmed.mobile.data.network.ApiClient
import com.appointmed.mobile.util.NetworkUtils
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginPresenter(
    private var view: LoginContract.View?,
    private val context: Context
) : LoginContract.Presenter {

    private var selectedRole = "PATIENT"

    override fun onRoleSelected(role: String) {
        selectedRole = role
        view?.setRole(role)
    }

    override fun onLoginClicked(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            view?.showError("Please provide email and password.")
            return
        }

        if (!NetworkUtils.isOnline(context)) {
            view?.showError("No internet connection. Please try again.")
            return
        }

        view?.showLoading()

        val request = LoginRequest(email = email, password = password, role = selectedRole)

        ApiClient.create(context).login(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    val auth = response.body()
                    if (auth != null && !auth.token.isNullOrEmpty()) {
                        Prefs(context).saveAuth(auth, password)
                        view?.navigateToDashboard(auth.toUser(password))
                    } else {
                        view?.showError("Invalid server response.")
                    }
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
            if (message.isNotEmpty()) message else "Unable to complete login."
        } catch (exception: Exception) {
            "Unable to complete login."
        }
    }
}
