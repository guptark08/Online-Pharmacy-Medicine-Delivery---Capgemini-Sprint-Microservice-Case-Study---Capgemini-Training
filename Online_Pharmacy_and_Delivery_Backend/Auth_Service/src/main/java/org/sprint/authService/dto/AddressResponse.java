package org.sprint.authService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long id;
    private String street_address;
    private String city;
    private String state;
    private int pincode;
    private boolean isDefault;
    private String contactPhone;
}
