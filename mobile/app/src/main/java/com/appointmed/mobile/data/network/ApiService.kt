package com.appointmed.mobile.data.network

import com.appointmed.mobile.data.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ===== Auth / Users =====
    @POST("users/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("users/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @PUT("users/{id}/change-password")
    fun changePassword(@Path("id") id: Long, @Body request: ChangePasswordRequest): Call<Void>

    @GET("users/{id}")
    fun getUser(@Path("id") id: Long): Call<User>

    @PUT("users/{id}")
    fun updateUser(@Path("id") id: Long, @Body user: User): Call<User>

    @GET("users/{id}/stats")
    fun getUserStats(@Path("id") id: Long): Call<StatsResponse>

    // ===== Doctors =====
    @GET("doctors")
    fun getDoctors(@Query("spec") spec: String? = null): Call<List<DoctorResponse>>

    @GET("doctors/{id}")
    fun getDoctor(@Path("id") id: Long): Call<DoctorResponse>

    @GET("doctors/search")
    fun searchDoctors(@Query("q") q: String, @Query("spec") spec: String? = null): Call<List<DoctorResponse>>

    @GET("doctors/user/{userId}")
    fun getDoctorByUserId(@Path("userId") userId: Long): Call<DoctorResponse>

    @GET("doctors/specializations")
    fun getSpecializations(): Call<List<String>>

    @GET("doctors/{id}/slots-status")
    fun getSlotsWithStatus(@Path("id") id: Long, @Query("date") date: String): Call<List<SlotStatus>>

    @GET("doctors/{id}/slots")
    fun getAvailableSlots(@Path("id") id: Long, @Query("date") date: String): Call<List<String>>

    @PUT("doctors/{id}/slots/toggle")
    fun toggleSlot(@Path("id") id: Long, @Query("date") date: String, @Query("time") time: String): Call<Map<String, Any>>

    @PUT("doctors/{id}/profile")
    fun updateDoctorProfile(@Path("id") id: Long, @Body req: DoctorProfileUpdateRequest): Call<DoctorResponse>

    // ===== Appointments =====
    @POST("appointments")
    fun createAppointment(@Body request: AppointmentRequest): Call<Appointment>

    @GET("appointments/patient/{id}")
    fun getPatientAppointments(@Path("id") id: Long, @Query("status") status: String? = null): Call<List<Appointment>>

    @GET("appointments/doctor/{id}")
    fun getDoctorAppointments(@Path("id") id: Long, @Query("date") date: String? = null): Call<List<Appointment>>

    @PUT("appointments/{id}/cancel")
    fun cancelAppointment(@Path("id") id: Long): Call<Appointment>

    @PUT("appointments/{id}/confirm")
    fun confirmAppointment(@Path("id") id: Long): Call<Appointment>

    @PUT("appointments/{id}/complete")
    fun completeAppointment(@Path("id") id: Long): Call<Appointment>

    // ===== Notifications =====
    @GET("notifications/user/{id}")
    fun getNotifications(@Path("id") id: Long): Call<List<NotificationItem>>

    @PUT("notifications/{id}/read")
    fun markNotificationRead(@Path("id") id: Long): Call<NotificationItem>

    @PUT("notifications/user/{id}/read-all")
    fun markAllNotificationsRead(@Path("id") id: Long): Call<Void>
}
