package com.appointmed.controller;

import com.appointmed.dto.DoctorResponse;
import com.appointmed.model.DoctorSchedule;
import com.appointmed.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired private DoctorService doctorService;

    @GetMapping
    public List<DoctorResponse> getAll(@RequestParam(required = false) String spec) {
        if (spec != null && !spec.isBlank()) {
            return doctorService.searchDoctors(null, spec);
        }
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/search")
    public List<DoctorResponse> search(@RequestParam(required = false) String q,
                                        @RequestParam(required = false) String spec) {
        return doctorService.searchDoctors(q, spec);
    }

    @GetMapping("/specializations")
    public List<String> getSpecializations() {
        return doctorService.getAllSpecializations();
    }

    @GetMapping("/{id}/slots")
    public List<String> getAvailableSlots(@PathVariable Long id,
                                           @RequestParam String date) {
        return doctorService.getAvailableSlots(id, LocalDate.parse(date));
    }

    @GetMapping("/{id}/schedule")
    public List<DoctorSchedule> getSchedule(@PathVariable Long id) {
        return doctorService.getSchedule(id);
    }

    @PutMapping("/{id}/schedule")
    public List<DoctorSchedule> updateSchedule(@PathVariable Long id,
                                                @RequestBody List<DoctorSchedule> schedules) {
        return doctorService.updateSchedule(id, schedules);
    }

    @GetMapping("/{id}/slots-status")
    public List<java.util.Map<String, Object>> getSlotsWithStatus(@PathVariable Long id,
                                                                    @RequestParam String date) {
        return doctorService.getSlotsWithStatus(id, LocalDate.parse(date));
    }

    @PutMapping("/{id}/slots/toggle")
    public java.util.Map<String, Object> toggleSlot(@PathVariable Long id,
                                                     @RequestParam String date,
                                                     @RequestParam String time) {
        return doctorService.toggleBlockedSlot(id, LocalDate.parse(date), java.time.LocalTime.parse(time));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DoctorResponse> getByUserId(@PathVariable Long userId) {
        var doc = doctorService.getDoctorByUserId(userId);
        return ResponseEntity.ok(doctorService.getDoctorById(doc.getId()));
    }
}
