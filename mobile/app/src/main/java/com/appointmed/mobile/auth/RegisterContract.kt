package com.appointmed.mobile.auth

interface RegisterContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showMessage(message: String)
        fun setRole(role: String)
        fun showSpecializations(specs: List<String>)
        fun clearFormAndFinish()
    }

    interface Presenter {
        fun onRoleSelected(role: String)
        fun onRegisterClicked(
            fullName: String, email: String, phone: String, address: String,
            gender: String, birthDate: String, specialization: String,
            licenseNumber: String, clinicAddress: String, consultationFee: String,
            password: String, confirmPassword: String, termsAccepted: Boolean
        )
        fun onDestroy()
    }
}
