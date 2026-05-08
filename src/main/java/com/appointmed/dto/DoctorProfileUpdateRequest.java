package com.appointmed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileUpdateRequest {
    private String specialization;
    private String licenseNumber;
    private String phone;
    private String clinicAddress;
    private BigDecimal consultationFee;
}
