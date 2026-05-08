package com.appointmed.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String specialization;

    @Column(unique = true)
    private String licenseNumber;

    private String phone;

    private String clinicAddress;

    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee = new BigDecimal("500.00");

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Doctor() {
    }

    public Doctor(Long id, String specialization, String licenseNumber, String phone, String clinicAddress, BigDecimal consultationFee, User user) {
        this.id = id;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
        this.clinicAddress = clinicAddress;
        this.consultationFee = consultationFee != null ? consultationFee : new BigDecimal("500.00");
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getPhone() {
        return phone;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public User getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setClinicAddress(String clinicAddress) {
        this.clinicAddress = clinicAddress;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
