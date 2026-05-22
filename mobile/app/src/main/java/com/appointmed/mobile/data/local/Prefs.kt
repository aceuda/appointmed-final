package com.appointmed.mobile.data.local

import android.content.Context
import com.appointmed.mobile.data.model.AuthResponse
import com.appointmed.mobile.data.model.User

class Prefs(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("appointmed_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = sharedPreferences.getString(KEY_TOKEN, null)

    fun saveAuth(auth: AuthResponse, password: String? = null) {
        saveToken(auth.token ?: "")
        saveUser(auth.toUser(password))
    }

    fun saveUser(user: User) {
        sharedPreferences.edit()
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_ROLE, user.role)
            .putString(KEY_USER_AVATAR, user.avatarUrl)
            .putString(KEY_USER_AVATAR_DATA, user.avatarData)
            .putString(KEY_USER_PASSWORD, user.password)
            .putString(KEY_USER_PHONE, user.phone)
            .putString(KEY_USER_ADDRESS, user.address)
            .putString(KEY_USER_GENDER, user.gender)
            .putString(KEY_USER_BIRTH_DATE, user.birthDate)
            .putString(KEY_USER_BLOOD_TYPE, user.bloodType)
            .putString(KEY_USER_SPECIALIZATION, user.specialization)
            .putString(KEY_USER_LICENSE_NUMBER, user.licenseNumber)
            .putString(KEY_USER_CLINIC_ADDRESS, user.clinicAddress)
            .putFloat(KEY_USER_FEE, user.consultationFee.toFloat())
            .apply()
    }

    fun savePatientDetails(phone: String?, address: String?, birthDate: String?, bloodType: String?) {
        sharedPreferences.edit()
            .putString(KEY_USER_PHONE, phone)
            .putString(KEY_USER_ADDRESS, address)
            .putString(KEY_USER_BIRTH_DATE, birthDate)
            .putString(KEY_USER_BLOOD_TYPE, bloodType)
            .apply()
    }

    fun getUser(): User {
        return User(
            id = sharedPreferences.getLong(KEY_USER_ID, 0),
            name = sharedPreferences.getString(KEY_USER_NAME, "") ?: "",
            email = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: "",
            password = sharedPreferences.getString(KEY_USER_PASSWORD, "") ?: "",
            role = sharedPreferences.getString(KEY_USER_ROLE, "") ?: "",
            avatarUrl = sharedPreferences.getString(KEY_USER_AVATAR, null),
            avatarData = sharedPreferences.getString(KEY_USER_AVATAR_DATA, null),
            phone = sharedPreferences.getString(KEY_USER_PHONE, null),
            address = sharedPreferences.getString(KEY_USER_ADDRESS, null),
            gender = sharedPreferences.getString(KEY_USER_GENDER, null),
            birthDate = sharedPreferences.getString(KEY_USER_BIRTH_DATE, null),
            bloodType = sharedPreferences.getString(KEY_USER_BLOOD_TYPE, null),
            specialization = sharedPreferences.getString(KEY_USER_SPECIALIZATION, null),
            licenseNumber = sharedPreferences.getString(KEY_USER_LICENSE_NUMBER, null),
            clinicAddress = sharedPreferences.getString(KEY_USER_CLINIC_ADDRESS, null),
            consultationFee = sharedPreferences.getFloat(KEY_USER_FEE, 0f).toDouble()
        )
    }

    fun getPatientPhone(): String? = sharedPreferences.getString(KEY_USER_PHONE, "")
    fun getPatientAddress(): String? = sharedPreferences.getString(KEY_USER_ADDRESS, "")
    fun getPatientBirthDate(): String? = sharedPreferences.getString(KEY_USER_BIRTH_DATE, "")
    fun getPatientBloodType(): String? = sharedPreferences.getString(KEY_USER_BLOOD_TYPE, "")

    fun isLoggedIn(): Boolean = getToken() != null && getUser().id != 0L

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "key_token"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_USER_AVATAR = "key_user_avatar"
        private const val KEY_USER_AVATAR_DATA = "key_user_avatar_data"
        private const val KEY_USER_PASSWORD = "key_user_password"
        private const val KEY_USER_PHONE = "key_user_phone"
        private const val KEY_USER_ADDRESS = "key_user_address"
        private const val KEY_USER_GENDER = "key_user_gender"
        private const val KEY_USER_BIRTH_DATE = "key_user_birth_date"
        private const val KEY_USER_BLOOD_TYPE = "key_user_blood_type"
        private const val KEY_USER_SPECIALIZATION = "key_user_specialization"
        private const val KEY_USER_LICENSE_NUMBER = "key_user_license_number"
        private const val KEY_USER_CLINIC_ADDRESS = "key_user_clinic_address"
        private const val KEY_USER_FEE = "key_user_fee"
    }
}
