package com.appointmed.repository;

import com.appointmed.model.BlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, Long> {
    List<BlockedSlot> findByDoctorIdAndBlockedDate(Long doctorId, LocalDate date);
    Optional<BlockedSlot> findByDoctorIdAndBlockedDateAndBlockedTime(Long doctorId, LocalDate date, LocalTime time);
    boolean existsByDoctorIdAndBlockedDateAndBlockedTime(Long doctorId, LocalDate date, LocalTime time);
    void deleteByDoctorIdAndBlockedDateAndBlockedTime(Long doctorId, LocalDate date, LocalTime time);
}
