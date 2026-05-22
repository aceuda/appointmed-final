package com.appointmed.mobile.auth

import com.appointmed.mobile.data.model.User

interface LoginContract {
    interface View {
        fun setRole(role: String)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showMessage(message: String)
        fun navigateToDashboard(user: User)
    }

    interface Presenter {
        fun onRoleSelected(role: String)
        fun onLoginClicked(email: String, password: String)
        fun onDestroy()
    }
}
