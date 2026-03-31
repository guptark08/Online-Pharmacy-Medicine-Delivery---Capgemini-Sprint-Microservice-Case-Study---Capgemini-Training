package com.pharmacy.admin.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.enums.PrescriptionStatus;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByStatusOrderByUploadedAtAsc(PrescriptionStatus status);

    List<Prescription> findByUserId(Long userId);

    Page<Prescription> findAllByOrderByUploadedAtDesc(Pageable pageable);

    long countByStatus(PrescriptionStatus status);

    long countByStatusAndUploadedAtBetween(PrescriptionStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.uploadedAt BETWEEN :start AND :end")
    long countUploadedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Prescription> findByOrderId(Long orderId);
}
