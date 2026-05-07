package com.appointmed.service;

import com.appointmed.dto.LoginRequest;
import com.appointmed.dto.RegisterRequest;
import com.appointmed.model.Doctor;
import com.appointmed.model.Patient;
import com.appointmed.model.User;
import com.appointmed.repository.DoctorRepository;
import com.appointmed.repository.PatientRepository;
import com.appointmed.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    // basic CRUD operations used by generic endpoints
    public User createUser(User user) {
        return userRepository.save(user);
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(userDetails.getPassword());
        }
        user.setRole(userDetails.getRole());
        user.setAvatarUrl(userDetails.getAvatarUrl());
        user.setAvatarData(userDetails.getAvatarData());
        if (userDetails.getPhone() != null) user.setPhone(userDetails.getPhone());
        if (userDetails.getAddress() != null) user.setAddress(userDetails.getAddress());
        if (userDetails.getGender() != null) user.setGender(userDetails.getGender());
        if (userDetails.getBirthDate() != null) user.setBirthDate(userDetails.getBirthDate());
        if (userDetails.getBloodType() != null) user.setBloodType(userDetails.getBloodType());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered.");
        }

        String role = request.getRole().toUpperCase(Locale.ROOT);

        if (!role.equals("PATIENT") && !role.equals("DOCTOR") && !role.equals("ADMIN")) {
            throw new RuntimeException("Invalid role. Only PATIENT, DOCTOR or ADMIN allowed.");
        }

        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(role);
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());

        User savedUser = userRepository.save(user);

        if (role.equals("DOCTOR")) {
            Doctor doctor = new Doctor();
            doctor.setSpecialization(request.getSpecialization());
            doctor.setLicenseNumber(request.getLicenseNumber());
            doctor.setPhone(request.getPhone());
            doctor.setClinicAddress(request.getClinicAddress());
            doctor.setUser(savedUser);
            doctorRepository.save(doctor);
        } else if (role.equals("PATIENT")) {
            Patient patient = new Patient();
            patient.setUser(savedUser);
            patient.setFirstName(request.getFullName());
            patient.setGender(request.getGender());
            patientRepository.save(patient);
        }

        return savedUser;
    }

    public Optional<User> login(LoginRequest request) {
        String role = request.getRole().toUpperCase(Locale.ROOT);

        Optional<User> userOpt = userRepository.findByEmailAndRole(request.getEmail(), role);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        String storedPassword = user.getPassword();
        String inputPassword = request.getPassword();

        if (storedPassword == null || inputPassword == null) {
            return Optional.empty();
        }

        // Direct plain-text password comparison
        if (storedPassword.equals(inputPassword)) {
            return Optional.of(user);
        }

        return Optional.empty();
    }
}