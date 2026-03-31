package com.orderanddelivery.integration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.orderanddelivery.exception.ExternalServiceException;
import com.orderanddelivery.exception.ResourceNotFoundException;
import com.orderanddelivery.requestDTO.UserAddressRequest;
import com.orderanddelivery.responseDTO.UserAddressResponse;

@Component
public class AddressClient {

    private final AuthFeignClient authFeignClient;

    public AddressClient(AuthFeignClient authFeignClient) {
        this.authFeignClient = authFeignClient;
    }

    public List<UserAddressResponse> getCurrentUserAddresses(String bearerToken) {
        try {
            Map<String, Object> root = authFeignClient.getCurrentUserAddresses(toAuthorizationHeader(bearerToken));
            return parseAddressList(root == null ? null : root.get("data"));
        } catch (ResourceNotFoundException | ExternalServiceException | IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ExternalServiceException("Unable to fetch addresses from auth service", ex);
        }
    }

    public UserAddressResponse getAddressByIdForCurrentUser(Long addressId, String bearerToken) {
        if (addressId == null) {
            throw new IllegalArgumentException("Address ID is required");
        }

        List<UserAddressResponse> addresses = getCurrentUserAddresses(bearerToken);
        return addresses.stream()
                .filter(address -> addressId.equals(address.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    public UserAddressResponse addAddressForCurrentUser(UserAddressRequest request, String bearerToken) {
        validateAddressRequest(request);

        try {
            Map<String, Object> root = authFeignClient.addAddress(toAuthorizationHeader(bearerToken), toAuthPayload(request));
            return parseAddress(root == null ? null : root.get("data"));
        } catch (ResourceNotFoundException | ExternalServiceException | IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ExternalServiceException("Unable to add address using auth service", ex);
        }
    }

    private String toAuthorizationHeader(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("Authorization token is required");
        }
        return "Bearer " + bearerToken;
    }

    private void validateAddressRequest(UserAddressRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("New address details are required");
        }
        if (request.getStreetAddress() == null || request.getStreetAddress().isBlank()) {
            throw new IllegalArgumentException("Street address is required");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (request.getState() == null || request.getState().isBlank()) {
            throw new IllegalArgumentException("State is required");
        }
        if (request.getPincode() == null) {
            throw new IllegalArgumentException("Pincode is required");
        }
    }

    private Map<String, Object> toAuthPayload(UserAddressRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("street_address", request.getStreetAddress().trim());
        payload.put("city", request.getCity().trim());
        payload.put("state", request.getState().trim());
        payload.put("pincode", request.getPincode());
        payload.put("isDefault", Boolean.TRUE.equals(request.getIsDefault()));
        return payload;
    }

    private List<UserAddressResponse> parseAddressList(Object rawData) {
        if (rawData == null) {
            return List.of();
        }
        if (!(rawData instanceof List<?> values)) {
            throw new ExternalServiceException("Unexpected address list response from auth service");
        }

        List<UserAddressResponse> responses = new ArrayList<>();
        for (Object value : values) {
            responses.add(parseAddress(value));
        }
        return responses;
    }

    @SuppressWarnings("unchecked")
    private UserAddressResponse parseAddress(Object rawAddress) {
        if (!(rawAddress instanceof Map<?, ?> map)) {
            throw new ExternalServiceException("Unexpected address payload from auth service");
        }

        Map<String, Object> addressMap = (Map<String, Object>) map;

        return UserAddressResponse.builder()
                .id(toLong(addressMap.get("id")))
                .streetAddress(Objects.toString(addressMap.get("street_address"), null))
                .city(Objects.toString(addressMap.get("city"), null))
                .state(Objects.toString(addressMap.get("state"), null))
                .pincode(toInteger(addressMap.get("pincode")))
                .isDefault(toBoolean(addressMap.get("isDefault")))
                .contactPhone(Objects.toString(addressMap.get("contactPhone"), null))
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer intValue) {
            return intValue;
        }
        if (value instanceof Long longValue) {
            return longValue.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        if (value == null) {
            return Boolean.FALSE;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
