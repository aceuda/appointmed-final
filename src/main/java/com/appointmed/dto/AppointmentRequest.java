package com.appointmed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
    @NotNull private Long patientId;
    @NotNull private Long doctorId;
    @NotNull private LocalDate appointmentDate;
    @NotNull private LocalTime appointmentTime;
    private LocalTime endTime;
    private String reason;
    private BigDecimal fee;
}
