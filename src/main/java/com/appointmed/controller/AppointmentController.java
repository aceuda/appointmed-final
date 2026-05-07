package com.appointmed.controller;

import com.appointmed.dto.AppointmentRequest;
import com.appointmed.model.Appointment;
import com.appointmed.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> create(@RequestBody AppointmentRequest req) {
        return ResponseEntity.ok(appointmentService.createAppointment(req));
    }

    @GetMapping
    public List<Appointment> getAll() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> update(@PathVariable Long id, @RequestBody AppointmentRequest req) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, req));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Appointment> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.confirmAppointment(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Appointment> complete(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id));
    }

    @GetMapping("/patient/{patientId}")
    public List<Appointment> getByPatient(@PathVariable Long patientId,
                                           @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return appointmentService.getByPatientIdAndStatus(patientId, Appointment.Status.valueOf(status));
        }
        return appointmentService.getByPatientId(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> getByDoctor(@PathVariable Long doctorId,
                                          @RequestParam(required = false) String date) {
        if (date != null && !date.isBlank()) {
            return appointmentService.getByDoctorIdAndDate(doctorId, LocalDate.parse(date));
        }
        return appointmentService.getByDoctorId(doctorId);
    }
}
