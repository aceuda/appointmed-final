package com.appointmed.mobile.profile

import android.content.Context
import com.appointmed.mobile.data.local.Prefs
import com.appointmed.mobile.data.model.ChangePasswordRequest
import com.appointmed.mobile.data.model.User
import com.appointmed.mobile.data.network.ApiClient
import com.appointmed.mobile.util.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilePresenter(
    private var view: ProfileContract.View?,
    private val context: Context
) : ProfileContract.Presenter {

    override fun loadProfile() {
        val prefs = Prefs(context)
        if (!prefs.isLoggedIn()) {
            view?.navigateToLogin()
            return
        }

        // Load cached data first
        val user = prefs.getUser()
        view?.populateFields(
            user,
            prefs.getPatientPhone(),
            prefs.getPatientAddress(),
            prefs.getPatientBirthDate(),
            prefs.getPatientBloodType(),
            user.consultationFee
        )

        if (user.avatarData?.isNotEmpty() == true) {
            view?.setAvatarFromBase64(user.avatarData!!)
        } else if (user.avatarUrl?.isNotEmpty() == true) {
            view?.setAvatarFromUrl(user.avatarUrl!!)
        } else {
            view?.setDefaultAvatar()
        }

        // Immediately fetch updated data from network
        if (NetworkUtils.isOnline(context)) {
            ApiClient.create(context).getUser(user.id).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        val updatedUser = response.body()!!
                        
                        // Save updated profile and patient details to Prefs
                        prefs.saveUser(updatedUser)
                        if (updatedUser.role == "PATIENT") {
                            prefs.savePatientDetails(
                                updatedUser.phone,
                                updatedUser.address,
                                updatedUser.birthDate,
                                updatedUser.bloodType
                            )
                        }

                        // Update UI with newly fetched data
                        view?.populateFields(
                            updatedUser,
                            prefs.getPatientPhone(),
                            prefs.getPatientAddress(),
                            prefs.getPatientBirthDate(),
                            prefs.getPatientBloodType(),
                            updatedUser.consultationFee
                        )

                        if (updatedUser.avatarData?.isNotEmpty() == true) {
                            view?.setAvatarFromBase64(updatedUser.avatarData!!)
                        } else if (updatedUser.avatarUrl?.isNotEmpty() == true) {
                            view?.setAvatarFromUrl(updatedUser.avatarUrl!!)
                        } else {
                            view?.setDefaultAvatar()
                        }
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    // Fail silently, user still sees cached data
                }
            })
        }
    }

    override fun saveProfile(name: String, email: String, phone: String, address: String, birthDate: String, bloodType: String, consultationFee: String, avatarData: String?) {
        if (name.isEmpty() || email.isEmpty()) {
            view?.showToast("Name and email are required.")
            return
        }

        val prefs = Prefs(context)
        val user = prefs.getUser()

        if (user.role == "PATIENT") {
            if (phone.isEmpty() || address.isEmpty() || birthDate.isEmpty() || bloodType.isEmpty()) {
                view?.showToast("Please fill in all personal details.")
                return
            }
        } else if (user.role == "DOCTOR") {
            if (consultationFee.isEmpty()) {
                view?.showToast("Consultation fee is required.")
                return
            }
        }

        if (!NetworkUtils.isOnline(context)) {
            view?.showToast("No internet connection. Please try again later.")
            return
        }

        view?.showProfileLoading(true)

        val newAvatarData = if (avatarData.isNullOrEmpty()) user.avatarData else avatarData
        val cleanedAvatar = if (newAvatarData.isNullOrEmpty()) null else newAvatarData

        val requestUser = user.copy(
            name = name,
            email = email,
            avatarData = cleanedAvatar,
            avatarUrl = null,
            consultationFee = consultationFee.toDoubleOrNull() ?: user.consultationFee
        )

        ApiClient.create(context).updateUser(user.id, requestUser).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!
                    
                    if (updatedUser.role == "DOCTOR") {
                        view?.showProfileLoading(true) // Keep loading
                        val newFee = requestUser.consultationFee
                        
                        // First get doctor profile to get doctorId
                        ApiClient.create(context).getDoctorByUserId(updatedUser.id).enqueue(object : Callback<com.appointmed.mobile.data.model.DoctorResponse> {
                            override fun onResponse(call: Call<com.appointmed.mobile.data.model.DoctorResponse>, docRes: Response<com.appointmed.mobile.data.model.DoctorResponse>) {
                                if (docRes.isSuccessful && docRes.body() != null) {
                                    val doctorId = docRes.body()!!.id
                                    val req = com.appointmed.mobile.data.model.DoctorProfileUpdateRequest(consultationFee = newFee)
                                    
                                    ApiClient.create(context).updateDoctorProfile(doctorId, req).enqueue(object : Callback<com.appointmed.mobile.data.model.DoctorResponse> {
                                        override fun onResponse(call: Call<com.appointmed.mobile.data.model.DoctorResponse>, updateRes: Response<com.appointmed.mobile.data.model.DoctorResponse>) {
                                            view?.showProfileLoading(false)
                                            val finalUser = updatedUser.copy(consultationFee = newFee)
                                            Prefs(context).saveUser(finalUser)
                                            view?.showProfileUpdateSuccess()
                                            loadProfile()
                                        }
                                        override fun onFailure(call: Call<com.appointmed.mobile.data.model.DoctorResponse>, t: Throwable) {
                                            view?.showProfileLoading(false)
                                            view?.showProfileUpdateError("Updated user but failed to update doctor fee.")
                                        }
                                    })
                                } else {
                                    view?.showProfileLoading(false)
                                    view?.showProfileUpdateError("Updated user but failed to fetch doctor profile.")
                                }
                            }
                            override fun onFailure(call: Call<com.appointmed.mobile.data.model.DoctorResponse>, t: Throwable) {
                                view?.showProfileLoading(false)
                                view?.showProfileUpdateError("Network error updating doctor profile.")
                            }
                        })
                    } else {
                        view?.showProfileLoading(false)
                        Prefs(context).saveUser(updatedUser)
                        Prefs(context).savePatientDetails(phone, address, birthDate, bloodType)
                        view?.showProfileUpdateSuccess()
                        loadProfile()
                    }
                } else {
                    view?.showProfileLoading(false)
                    view?.showProfileUpdateError("Unable to update profile. Please try again.")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                view?.showProfileLoading(false)
                view?.showProfileUpdateError("Unable to update profile. ${t.localizedMessage}")
            }
        })
    }

    override fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            view?.showPasswordError("All password fields are required.")
            return
        }

        if (newPassword.length < 4) {
            view?.showPasswordError("New password must be at least 4 characters.")
            return
        }

        if (newPassword != confirmPassword) {
            view?.showPasswordError("New passwords do not match.")
            return
        }

        val prefs = Prefs(context)
        val user = prefs.getUser()

        if (currentPassword != user.password) {
            view?.showPasswordError("Current password is incorrect.")
            return
        }

        if (!NetworkUtils.isOnline(context)) {
            view?.showPasswordError("No internet connection. Please try again later.")
            return
        }

        view?.showPasswordLoading(true)

        val request = ChangePasswordRequest(
            currentPassword = currentPassword,
            newPassword = newPassword
        )

        ApiClient.create(context).changePassword(user.id, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                view?.showPasswordLoading(false)
                if (response.isSuccessful) {
                    Prefs(context).saveUser(user.copy(password = newPassword))
                    view?.showPasswordSuccess("Password changed successfully!")
                    view?.clearPasswordFields()
                } else {
                    view?.showPasswordError("Unable to update password. Please try again.")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                view?.showPasswordLoading(false)
                view?.showPasswordError("Unable to connect to server. ${t.localizedMessage}")
            }
        })
    }

    override fun onLogoutConfirmed() {
        Prefs(context).clear()
        view?.showToast("Logged out successfully.")
        view?.navigateToLogin()
    }

    override fun onHomeClicked() {
        view?.navigateToHome()
    }

    override fun onScheduleClicked() {
        view?.navigateToAppointments()
    }

    override fun onRecordsClicked() {
        view?.navigateToRecords()
    }

    override fun onDestroy() {
        view = null
    }
}
