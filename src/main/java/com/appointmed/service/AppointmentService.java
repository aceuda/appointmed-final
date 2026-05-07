package com.appointmed.service;

import com.appointmed.dto.AppointmentRequest;
import com.appointmed.exception.ResourceNotFoundException;
import com.appointmed.model.Appointment;
import com.appointmed.model.Doctor;
import com.appointmed.model.Notification;
import com.appointmed.model.User;
import com.appointmed.repository.AppointmentRepository;
import com.appointmed.repository.BlockedSlotRepository;
import com.appointmed.repository.DoctorRepository;
import com.appointmed.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired private AppointmentRepository appointmentRepo;
    @Autowired private BlockedSlotRepository blockedSlotRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private DoctorRepository doctorRepo;
    @Autowired private NotificationService notificationService;

    @Transactional
    public Appointment createAppointment(AppointmentRequest req) {
        // Prevent double-booking: check if slot is already taken
        boolean alreadyBooked = appointmentRepo
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        req.getDoctorId(), req.getAppointmentDate(),
                        req.getAppointmentTime(), Appointment.Status.CANCELLED);
        if (alreadyBooked) {
            throw new IllegalStateException("This time slot is already booked. Please select another slot.");
        }

        // Prevent booking on a slot the doctor has blocked
        boolean isBlocked = blockedSlotRepo.existsByDoctorIdAndBlockedDateAndBlockedTime(
                req.getDoctorId(), req.getAppointmentDate(), req.getAppointmentTime());
        if (isBlocked) {
            throw new IllegalStateException("This time slot is not available. The doctor has blocked this slot.");
        }

        User patient = userRepo.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Doctor doctor = doctorRepo.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(req.getAppointmentDate())
                .appointmentTime(req.getAppointmentTime())
                .endTime(req.getEndTime())
                .reason(req.getReason())
                .fee(req.getFee())
                .status(Appointment.Status.PENDING)
                .paymentStatus(Appointment.PaymentStatus.UNPAID)
                .build();

        Appointment saved = appointmentRepo.save(appointment);

        // Notify doctor
        notificationService.createNotification(
                doctor.getUser(),
                "New Appointment Request",
                "Patient " + patient.getName() + " has requested an appointment on " + req.getAppointmentDate(),
                Notification.Type.APPOINTMENT
        );

        return saved;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public Appointment getById(Long id) {
        return appointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }

    public List<Appointment> getByPatientId(Long patientId) {
        return appointmentRepo.findByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    public List<Appointment> getByDoctorId(Long doctorId) {
        return appointmentRepo.findByDoctorIdOrderByAppointmentDateDesc(doctorId);
    }

    public List<Appointment> getByDoctorIdAndDate(Long doctorId, LocalDate date) {
        return appointmentRepo.findByDoctorIdAndAppointmentDate(doctorId, date);
    }

    public List<Appointment> getByPatientIdAndStatus(Long patientId, Appointment.Status status) {
        return appointmentRepo.findByPatientIdAndStatus(patientId, status);
    }

    @Transactional
    public Appointment cancelAppointment(Long id) {
        Appointment appt = getById(id);
        appt.setStatus(Appointment.Status.CANCELLED);
        return appointmentRepo.save(appt);
    }

    @Transactional
    public Appointment confirmAppointment(Long id) {
        Appointment appt = getById(id);
        appt.setStatus(Appointment.Status.CONFIRMED);
        Appointment saved = appointmentRepo.save(appt);

        notificationService.createNotification(
                appt.getPatient(),
                "Appointment Confirmed",
                "Your appointment on " + appt.getAppointmentDate() + " at " + appt.getAppointmentTime() + " has been confirmed.",
                Notification.Type.APPOINTMENT
        );

        return saved;
    }

    @Transactional
    public Appointment completeAppointment(Long id) {
        Appointment appt = getById(id);
        appt.setStatus(Appointment.Status.COMPLETED);
        return appointmentRepo.save(appt);
    }

    @Transactional
    public Appointment updateAppointment(Long id, AppointmentRequest req) {
        Appointment appt = getById(id);
        if (req.getAppointmentDate() != null) appt.setAppointmentDate(req.getAppointmentDate());
        if (req.getAppointmentTime() != null) appt.setAppointmentTime(req.getAppointmentTime());
        if (req.getEndTime() != null) appt.setEndTime(req.getEndTime());
        if (req.getReason() != null) appt.setReason(req.getReason());
        if (req.getFee() != null) appt.setFee(req.getFee());
        return appointmentRepo.save(appt);
    }

    public long countUnpaidByPatientId(Long patientId) {
        return appointmentRepo.countByPatientIdAndPaymentStatus(patientId, Appointment.PaymentStatus.UNPAID);
    }
}
