package com.appointmed.service;

import com.appointmed.model.MedicalRecord;
import com.appointmed.model.Prescription;
import com.appointmed.repository.MedicalRecordRepository;
import com.appointmed.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordService {

    @Autowired private MedicalRecordRepository recordRepo;
    @Autowired private PrescriptionRepository prescriptionRepo;

    // Medical Records
    public MedicalRecord createRecord(MedicalRecord record) { return recordRepo.save(record); }
    public List<MedicalRecord> getPatientRecords(Long patientId) { return recordRepo.findByPatientIdOrderByCreatedAtDesc(patientId); }
    public List<MedicalRecord> getDoctorRecords(Long doctorId) { return recordRepo.findByDoctorIdOrderByCreatedAtDesc(doctorId); }
    public long countPatientRecords(Long patientId) { return recordRepo.countByPatientId(patientId); }
    public MedicalRecord getRecordById(Long id) { return recordRepo.findById(id).orElseThrow(() -> new RuntimeException("Record not found")); }

    // Prescriptions
    public Prescription createPrescription(Prescription p) { return prescriptionRepo.save(p); }
    public List<Prescription> getPatientPrescriptions(Long patientId) { return prescriptionRepo.findByPatientIdOrderByCreatedAtDesc(patientId); }
    public List<Prescription> getDoctorPrescriptions(Long doctorId) { return prescriptionRepo.findByDoctorIdOrderByCreatedAtDesc(doctorId); }
    public long countActivePrescriptions(Long patientId) { return prescriptionRepo.countByPatientIdAndStatus(patientId, Prescription.Status.ACTIVE); }
    public Prescription getPrescriptionById(Long id) { return prescriptionRepo.findById(id).orElseThrow(() -> new RuntimeException("Prescription not found")); }
    public Prescription updatePrescription(Long id, Prescription updated) {
        Prescription p = getPrescriptionById(id);
        if (updated.getStatus() != null) p.setStatus(updated.getStatus());
        if (updated.getNotes() != null) p.setNotes(updated.getNotes());
        return prescriptionRepo.save(p);
    }
}
