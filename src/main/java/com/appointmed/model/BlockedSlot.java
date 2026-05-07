package com.appointmed.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "blocked_slots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_doctor_date_time_block", columnNames = {"doctor_id", "blockedDate", "blockedTime"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate blockedDate;

    @Column(nullable = false)
    private LocalTime blockedTime;
}
