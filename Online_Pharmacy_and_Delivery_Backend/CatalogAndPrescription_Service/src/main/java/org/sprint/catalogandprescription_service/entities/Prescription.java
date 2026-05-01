package org.sprint.catalogandprescription_service.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Customer who uploaded — stored as userId from JWT (not a FK to User entity
	// since Users are in a different service/database)
	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "file_path", nullable = false)
	private String filePath;

	@Column(name = "file_type", length = 20)
	private String fileType;

	@Column(name = "file_size")
	private Long fileSize;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PrescriptionStatus status = PrescriptionStatus.PENDING;

	@Column(name = "review_notes", length = 500)
	private String reviewNotes;

	@Column(name = "reviewed_by")
	private Long reviewedBy;

	@Column(name = "reviewed_at")
	private LocalDateTime reviewedAt;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "doctor_name")
	private String doctorName;

	@Column(name = "doctor_reg_number")
	private String doctorRegNumber;

	@Builder.Default
	@Column(name = "user_notified", nullable = false)
	private Boolean userNotified = false;

	@Column(name = "uploaded_at", updatable = false)
	private LocalDateTime uploadedAt;

	@PrePersist
	public void prePersist() {
		this.uploadedAt = LocalDateTime.now();
	}

	public enum PrescriptionStatus {
		PENDING, APPROVED, REJECTED, EXPIRED
	}
}