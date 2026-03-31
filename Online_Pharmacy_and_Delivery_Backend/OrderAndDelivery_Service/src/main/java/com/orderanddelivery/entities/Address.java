package com.orderanddelivery.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId; // from JWT (not a FK, different service)

	@Column(nullable = false)
	private String fullName;

	@Column(nullable = false)
	private String phone;

	@Column(nullable = false)
	private String line1;

	private String line2;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String state;

	@Column(nullable = false, length = 10)
	private String pincode;

	private boolean isDefault;
}