package com.orderanddelivery.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressResponse {

    private Long id;
    private String streetAddress;
    private String city;
    private String state;
    private Integer pincode;
    private Boolean isDefault;
    private String contactPhone;
}
