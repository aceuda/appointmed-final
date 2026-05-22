package com.appointmed.mobile.profile

import com.appointmed.mobile.data.model.User

interface ProfileContract {
    interface View {
        fun populateFields(user: User, phone: String?, address: String?, birthDate: String?, bloodType: String?, consultationFee: Double)
        fun showProfileLoading(isLoading: Boolean)
        fun showProfileUpdateSuccess()
        fun showProfileUpdateError(message: String)
        fun showPasswordLoading(isLoading: Boolean)
        fun showPasswordError(message: String)
        fun showPasswordSuccess(message: String)
        fun clearPasswordFields()
        fun showLogoutConfirmation()
        fun navigateToLogin()
        fun navigateToHome()
        fun navigateToAppointments()
        fun navigateToRecords()
        fun showToast(message: String)
        fun setAvatarFromBase64(data: String)
        fun setAvatarFromUrl(url: String)
        fun setDefaultAvatar()
    }

    interface Presenter {
        fun loadProfile()
        fun saveProfile(name: String, email: String, phone: String, address: String, birthDate: String, bloodType: String, consultationFee: String, avatarData: String?)
        fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String)
        fun onLogoutConfirmed()
        fun onHomeClicked()
        fun onScheduleClicked()
        fun onRecordsClicked()
        fun onDestroy()
    }
}
