package com.appointmed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String specialization;
    private String licenseNumber;
    private String phone;
    private String clinicAddress;
    private String avatarUrl;
    private boolean available;
    private double rating;
    private int reviews;
}
