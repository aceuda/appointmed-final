package com.appointmed.service;

import com.appointmed.dto.DoctorProfileUpdateRequest;
import com.appointmed.dto.DoctorResponse;
import com.appointmed.exception.ResourceNotFoundException;
import com.appointmed.model.Appointment;
import com.appointmed.model.BlockedSlot;
import com.appointmed.model.Doctor;
import com.appointmed.model.DoctorSchedule;
import com.appointmed.repository.AppointmentRepository;
import com.appointmed.repository.BlockedSlotRepository;
import com.appointmed.repository.DoctorRepository;
import com.appointmed.repository.DoctorScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired private DoctorRepository doctorRepo;
    @Autowired private DoctorScheduleRepository scheduleRepo;
    @Autowired private AppointmentRepository appointmentRepo;
    @Autowired private BlockedSlotRepository blockedSlotRepo;

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DoctorResponse> searchDoctors(String query, String specialization) {
        List<Doctor> all = doctorRepo.findAll();
        return all.stream()
                .filter(d -> {
                    boolean matchQuery = query == null || query.isBlank()
                            || d.getUser().getName().toLowerCase().contains(query.toLowerCase())
                            || (d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(query.toLowerCase()));
                    boolean matchSpec = specialization == null || specialization.isBlank()
                            || specialization.equalsIgnoreCase("All Specialists")
                            || (d.getSpecialization() != null && d.getSpecialization().equalsIgnoreCase(specialization));
                    return matchQuery && matchSpec;
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DoctorResponse getDoctorById(Long id) {
        Doctor doc = doctorRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        return toResponse(doc);
    }

    public Doctor getDoctorEntityById(Long id) {
        return doctorRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user"));
    }

    public List<String> getAllSpecializations() {
        return doctorRepo.findAll().stream()
                .map(Doctor::getSpecialization)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<DoctorSchedule> getSchedule(Long doctorId) {
        return scheduleRepo.findByDoctorIdAndIsActiveTrue(doctorId);
    }

    @Transactional
    public List<DoctorSchedule> updateSchedule(Long doctorId, List<DoctorSchedule> schedules) {
        Doctor doctor = getDoctorEntityById(doctorId);
        scheduleRepo.deleteByDoctorId(doctorId);

        for (DoctorSchedule s : schedules) {
            s.setDoctor(doctor);
            s.setId(null);
        }
        return scheduleRepo.saveAll(schedules);
    }

    public List<String> getAvailableSlots(Long doctorId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorSchedule> schedules = scheduleRepo.findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);

        // Get existing appointments for that day (exclude cancelled)
        List<Appointment> existingAppts = appointmentRepo.findByDoctorIdAndAppointmentDateAndStatusNot(
                doctorId, date, Appointment.Status.CANCELLED);
        Set<LocalTime> bookedTimes = existingAppts.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        // Get blocked slots for that day
        Set<LocalTime> blockedTimes = blockedSlotRepo.findByDoctorIdAndBlockedDate(doctorId, date).stream()
                .map(BlockedSlot::getBlockedTime)
                .collect(Collectors.toSet());

        List<String> slots = new ArrayList<>();

        if (schedules.isEmpty()) {
            // No schedule saved yet — provide default weekday slots (Mon-Sat)
            if (dayOfWeek != DayOfWeek.SUNDAY) {
                // Morning: 8:00 AM - 12:00 PM
                LocalTime morning = LocalTime.of(8, 0);
                while (morning.isBefore(LocalTime.of(12, 0))) {
                    if (!bookedTimes.contains(morning) && !blockedTimes.contains(morning)) {
                        slots.add(morning.toString());
                    }
                    morning = morning.plusMinutes(30);
                }
                // Afternoon: 1:00 PM - 5:00 PM
                LocalTime afternoon = LocalTime.of(13, 0);
                while (afternoon.isBefore(LocalTime.of(17, 0))) {
                    if (!bookedTimes.contains(afternoon) && !blockedTimes.contains(afternoon)) {
                        slots.add(afternoon.toString());
                    }
                    afternoon = afternoon.plusMinutes(30);
                }
            }
        } else {
            // Use the doctor's saved schedule
            for (DoctorSchedule schedule : schedules) {
                LocalTime current = schedule.getStartTime();
                while (current.plusMinutes(schedule.getSlotDurationMinutes()).compareTo(schedule.getEndTime()) <= 0) {
                    if (!bookedTimes.contains(current) && !blockedTimes.contains(current)) {
                        slots.add(current.toString());
                    }
                    current = current.plusMinutes(schedule.getSlotDurationMinutes());
                }
            }
        }
        return slots;
    }

    public List<Map<String, Object>> getSlotsWithStatus(Long doctorId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorSchedule> schedules = scheduleRepo.findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);

        // Get existing non-cancelled appointments for that day
        List<Appointment> existingAppts = appointmentRepo.findByDoctorIdAndAppointmentDateAndStatusNot(
                doctorId, date, Appointment.Status.CANCELLED);
        Map<LocalTime, Appointment> bookedMap = new HashMap<>();
        for (Appointment a : existingAppts) {
            bookedMap.put(a.getAppointmentTime(), a);
        }

        // Get blocked slots for that day
        Set<LocalTime> blockedTimes = blockedSlotRepo.findByDoctorIdAndBlockedDate(doctorId, date).stream()
                .map(BlockedSlot::getBlockedTime)
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();

        if (schedules.isEmpty()) {
            // Default weekday slots (Mon-Sat)
            if (dayOfWeek != DayOfWeek.SUNDAY) {
                // Morning: 8:00 AM - 12:00 PM
                LocalTime morning = LocalTime.of(8, 0);
                while (morning.isBefore(LocalTime.of(12, 0))) {
                    result.add(buildSlotEntry(morning, bookedMap, blockedTimes));
                    morning = morning.plusMinutes(30);
                }
                // Afternoon: 1:00 PM - 5:00 PM
                LocalTime afternoon = LocalTime.of(13, 0);
                while (afternoon.isBefore(LocalTime.of(17, 0))) {
                    result.add(buildSlotEntry(afternoon, bookedMap, blockedTimes));
                    afternoon = afternoon.plusMinutes(30);
                }
            }
        } else {
            for (DoctorSchedule schedule : schedules) {
                LocalTime current = schedule.getStartTime();
                while (current.plusMinutes(schedule.getSlotDurationMinutes()).compareTo(schedule.getEndTime()) <= 0) {
                    result.add(buildSlotEntry(current, bookedMap, blockedTimes));
                    current = current.plusMinutes(schedule.getSlotDurationMinutes());
                }
            }
        }
        return result;
    }

    @Transactional
    public Map<String, Object> toggleBlockedSlot(Long doctorId, LocalDate date, LocalTime time) {
        Doctor doctor = getDoctorEntityById(doctorId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("time", time.toString());
        result.put("date", date.toString());

        Optional<BlockedSlot> existing = blockedSlotRepo.findByDoctorIdAndBlockedDateAndBlockedTime(doctorId, date, time);
        if (existing.isPresent()) {
            // Unblock: remove the blocked slot
            blockedSlotRepo.delete(existing.get());
            result.put("status", "available");
            result.put("action", "unblocked");
        } else {
            // Block: create a new blocked slot
            BlockedSlot blocked = BlockedSlot.builder()
                    .doctor(doctor)
                    .blockedDate(date)
                    .blockedTime(time)
                    .build();
            blockedSlotRepo.save(blocked);
            result.put("status", "blocked");
            result.put("action", "blocked");
        }
        return result;
    }

    private Map<String, Object> buildSlotEntry(LocalTime time, Map<LocalTime, Appointment> bookedMap, Set<LocalTime> blockedTimes) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("time", time.toString());
        if (bookedMap.containsKey(time)) {
            Appointment appt = bookedMap.get(time);
            entry.put("status", "booked");
            entry.put("patientName", appt.getPatient() != null ? appt.getPatient().getName() : "Patient");
            entry.put("appointmentId", appt.getId());
            entry.put("appointmentStatus", appt.getStatus().name());
        } else if (blockedTimes.contains(time)) {
            entry.put("status", "blocked");
        } else {
            entry.put("status", "available");
        }
        return entry;
    }

    @Transactional
    public DoctorResponse updateDoctorProfile(Long doctorId, DoctorProfileUpdateRequest req) {
        Doctor doctor = getDoctorEntityById(doctorId);
        if (req.getSpecialization() != null) doctor.setSpecialization(req.getSpecialization());
        if (req.getLicenseNumber() != null) doctor.setLicenseNumber(req.getLicenseNumber());
        if (req.getPhone() != null) doctor.setPhone(req.getPhone());
        if (req.getClinicAddress() != null) doctor.setClinicAddress(req.getClinicAddress());
        if (req.getConsultationFee() != null) doctor.setConsultationFee(req.getConsultationFee());
        doctorRepo.save(doctor);
        return toResponse(doctor);
    }

    private DoctorResponse toResponse(Doctor doc) {
        return DoctorResponse.builder()
                .id(doc.getId())
                .userId(doc.getUser().getId())
                .name(doc.getUser().getName())
                .email(doc.getUser().getEmail())
                .specialization(doc.getSpecialization())
                .licenseNumber(doc.getLicenseNumber())
                .phone(doc.getPhone())
                .clinicAddress(doc.getClinicAddress())
                .avatarUrl(doc.getUser().getAvatarData() != null ? doc.getUser().getAvatarData() : doc.getUser().getAvatarUrl())
                .available(true)
                .rating(4.5 + (doc.getId() % 5) * 0.1)
                .reviews(50 + (int)(doc.getId() * 17 % 200))
                .consultationFee(doc.getConsultationFee())
                .build();
    }
}
