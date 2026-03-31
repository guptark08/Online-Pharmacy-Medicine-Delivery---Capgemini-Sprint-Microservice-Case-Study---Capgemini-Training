package org.sprint.authService.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.authService.dao.AddressRepository;
import org.sprint.authService.dto.AddressRequest;
import org.sprint.authService.dto.AddressResponse;
import org.sprint.authService.entities.Address;
import org.sprint.authService.entities.User;
import org.sprint.authService.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserService userService;
    private final AddressRepository addressRepository;

    @Transactional
    public AddressResponse addAddressForCurrentUser(AddressRequest addressRequest) {
        User user = getCurrentAuthenticatedUser();

        if (addressRequest.isDefault()) {
            resetDefaultAddress(user.getId());
        }

        Address address = Address.builder()
                .street_address(addressRequest.getStreet_address().trim())
                .city(addressRequest.getCity().trim())
                .pincode(addressRequest.getPincode())
                .state(addressRequest.getState().trim())
                .isDefault(addressRequest.isDefault())
                .user(user)
                .build();

        return mapToAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddressForCurrentUser(Long addressId, AddressRequest addressRequest) {
        User user = getCurrentAuthenticatedUser();

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (addressRequest.isDefault()) {
            resetDefaultAddress(user.getId());
        }

        address.setStreet_address(addressRequest.getStreet_address().trim());
        address.setCity(addressRequest.getCity().trim());
        address.setPincode(addressRequest.getPincode());
        address.setState(addressRequest.getState().trim());
        address.setDefault(addressRequest.isDefault());

        return mapToAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddressForCurrentUser(Long id) {
        User user = getCurrentAuthenticatedUser();

        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddressResponses() {
        User currentUser = getCurrentAuthenticatedUser();

        return addressRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToAddressResponse)
                .toList();
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated");
        }

        return userService.getActiveByUsername(authentication.getName());
    }

    private void resetDefaultAddress(Long userId) {
        List<Address> existingDefaults = addressRepository.findByUserIdAndIsDefaultTrue(userId);
        if (existingDefaults.isEmpty()) {
            return;
        }
        for (Address existing : existingDefaults) {
            existing.setDefault(false);
        }
        addressRepository.saveAll(existingDefaults);
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street_address(address.getStreet_address())
                .city(address.getCity())
                .pincode(address.getPincode())
                .state(address.getState())
                .isDefault(address.isDefault())
                .contactPhone(address.getUser() == null ? null : address.getUser().getMobile())
                .build();
    }
}
