package org.sprint.authService.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sprint.authService.dto.AddressRequest;
import org.sprint.authService.dto.AddressResponse;
import org.sprint.authService.dto.ApiResponse;
import org.sprint.authService.services.AddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    @PostMapping({"", "/add"})
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(@Valid @RequestBody AddressRequest addressRequest) {
        AddressResponse created = addressService.addAddressForCurrentUser(addressRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(@PathVariable Long id,
                                                                      @Valid @RequestBody AddressRequest request) {
        AddressResponse updated = addressService.updateAddressForCurrentUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddressForCurrentUser(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Address deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddresses() {
        List<AddressResponse> addresses = addressService.getAllAddressResponses();
        return ResponseEntity.ok(ApiResponse.success("Addresses fetched successfully", addresses));
    }
}
