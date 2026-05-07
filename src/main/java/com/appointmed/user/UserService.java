package com.appointmed.user;

import com.appointmed.doctor.Doctor;
import com.appointmed.doctor.DoctorRepository;
import com.appointmed.patient.Patient;
import com.appointmed.patient.PatientRepository;
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
        user.setPassword(userDetails.getPassword());
        user.setRole(userDetails.getRole());
        user.setAvatarUrl(userDetails.getAvatarUrl());
        user.setAvatarData(userDetails.getAvatarData());
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
            patient.setFirstName(request.getFullName());
            patient.setGender(request.getGender());
            patientRepository.save(patient);
        } else if (role.equals("ADMIN")) {
            // no extra entity for admin, just user record
        }

        return savedUser;
    }

    public Optional<User> login(LoginRequest request) {
        String role = request.getRole().toUpperCase(Locale.ROOT);

        return userRepository.findByEmailAndRole(request.getEmail(), role)
                .filter(user -> user.getPassword().equals(request.getPassword()));
    }
}
