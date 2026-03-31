package org.sprint.catalogandprescription_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionResponseDTO {

	private Long id;
	private Long customerId;
	private String fileName;
	private String fileType;
	private Long fileSize;
	private String status; // "PENDING", "APPROVED", "REJECTED"
	private String reviewNotes;
	private LocalDateTime uploadedAt;
	private LocalDateTime reviewedAt;
}