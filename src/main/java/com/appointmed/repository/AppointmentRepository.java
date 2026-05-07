package com.appointmed.repository;

import com.appointmed.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByAppointmentDateDesc(Long doctorId);
    List<Appointment> findByPatientIdAndStatus(Long patientId, Appointment.Status status);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(Long doctorId, LocalDate date, Appointment.Status status);
    long countByDoctorIdAndAppointmentDateAndStatus(Long doctorId, LocalDate date, Appointment.Status status);
    long countByPatientIdAndPaymentStatus(Long patientId, Appointment.PaymentStatus paymentStatus);
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId, java.time.LocalDate date, java.time.LocalTime time, Appointment.Status status);
}
