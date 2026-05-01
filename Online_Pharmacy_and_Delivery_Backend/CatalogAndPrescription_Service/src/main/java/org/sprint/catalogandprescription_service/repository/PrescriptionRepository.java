package org.sprint.catalogandprescription_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sprint.catalogandprescription_service.entities.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByCustomerIdOrderByUploadedAtDesc(Long customerId);

    Optional<Prescription> findByIdAndCustomerId(Long id, Long customerId);

    List<Prescription> findByStatusOrderByUploadedAtAsc(Prescription.PrescriptionStatus status);

    long countByStatus(Prescription.PrescriptionStatus status);

    List<Prescription> findByCustomerIdAndStatusAndUserNotifiedFalse(Long customerId, Prescription.PrescriptionStatus status);
}
