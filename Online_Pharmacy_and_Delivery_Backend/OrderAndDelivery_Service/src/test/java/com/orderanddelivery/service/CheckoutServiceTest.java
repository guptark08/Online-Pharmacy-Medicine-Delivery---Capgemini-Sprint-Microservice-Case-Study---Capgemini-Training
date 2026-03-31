package com.orderanddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.orderanddelivery.entities.CartItem;
import com.orderanddelivery.entities.Order;
import com.orderanddelivery.enums.OrderStatus;
import com.orderanddelivery.exception.InvalidOrderStateException;
import com.orderanddelivery.exception.ResourceNotFoundException;
import com.orderanddelivery.integration.AddressClient;
import com.orderanddelivery.integration.CatalogClient;
import com.orderanddelivery.repository.CartItemRepository;
import com.orderanddelivery.repository.OrderRepository;
import com.orderanddelivery.requestDTO.CheckoutRequest;
import com.orderanddelivery.requestDTO.UserAddressRequest;
import com.orderanddelivery.responseDTO.OrderResponse;
import com.orderanddelivery.responseDTO.UserAddressResponse;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddressClient addressClient;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CheckoutService checkoutService;

    @Test
    void startCheckout_emptyCart_throwsInvalidOrderState() {
        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of());
        CheckoutRequest request = buildRequest(1L, null);

        InvalidOrderStateException ex = assertThrows(InvalidOrderStateException.class,
                () -> checkoutService.startCheckout(10L, "token-abc", request));

        assertTrue(ex.getMessage().toLowerCase().contains("empty"));
    }

    @Test
    void startCheckout_addressNotOwnedByUser_throwsResourceNotFound() {
        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(nonRxCartItem()));
        when(addressClient.getAddressByIdForCurrentUser(1L, "token-abc"))
                .thenThrow(new ResourceNotFoundException("Address not found"));
        CheckoutRequest request = buildRequest(1L, null);

        assertThrows(ResourceNotFoundException.class,
                () -> checkoutService.startCheckout(10L, "token-abc", request));
    }

    @Test
    void startCheckout_rxItemWithoutPrescriptionId_throwsInvalidState() {
        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(rxCartItem()));
        when(addressClient.getAddressByIdForCurrentUser(1L, "token-abc")).thenReturn(address());
        CheckoutRequest request = buildRequest(1L, null);

        InvalidOrderStateException ex = assertThrows(InvalidOrderStateException.class,
                () -> checkoutService.startCheckout(10L, "token-abc", request));

        assertTrue(ex.getMessage().contains("Prescription is required"));
    }

    @Test
    void startCheckout_prescriptionRejected_throwsInvalidState() {
        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(rxCartItem()));
        when(addressClient.getAddressByIdForCurrentUser(1L, "token-abc")).thenReturn(address());
        when(catalogClient.fetchPrescriptionStatusForUser(99L, "token-abc")).thenReturn("REJECTED");
        CheckoutRequest request = buildRequest(1L, 99L);

        InvalidOrderStateException ex = assertThrows(InvalidOrderStateException.class,
                () -> checkoutService.startCheckout(10L, "token-abc", request));

        assertTrue(ex.getMessage().contains("not approved"));
    }

    @Test
    void startCheckout_prescriptionApproved_setsStatusToApproved() {
        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(rxCartItem()));
        when(addressClient.getAddressByIdForCurrentUser(1L, "token-abc")).thenReturn(address());
        when(catalogClient.fetchPrescriptionStatusForUser(99L, "token-abc")).thenReturn("APPROVED");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        OrderResponse response = checkoutService.startCheckout(10L, "token-abc", buildRequest(1L, 99L));

        assertEquals(OrderStatus.PRESCRIPTION_APPROVED, response.getStatus());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertEquals(OrderStatus.PRESCRIPTION_APPROVED, captor.getValue().getStatus());
    }

    @Test
    void startCheckout_prescriptionPending_setsStatusToPrescriptionPending() {
        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(rxCartItem()));
        when(addressClient.getAddressByIdForCurrentUser(1L, "token-abc")).thenReturn(address());
        when(catalogClient.fetchPrescriptionStatusForUser(99L, "token-abc")).thenReturn("PENDING");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(101L);
            return order;
        });

        OrderResponse response = checkoutService.startCheckout(10L, "token-abc", buildRequest(1L, 99L));

        assertEquals(OrderStatus.PRESCRIPTION_PENDING, response.getStatus());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertEquals(OrderStatus.PRESCRIPTION_PENDING, captor.getValue().getStatus());
    }

    @Test
    void startCheckout_noRxItems_setsStatusToPaymentPending() {
        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(nonRxCartItem()));
        when(addressClient.getAddressByIdForCurrentUser(1L, "token-abc")).thenReturn(address());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(102L);
            return order;
        });

        OrderResponse response = checkoutService.startCheckout(10L, "token-abc", buildRequest(1L, null));

        assertEquals(OrderStatus.PAYMENT_PENDING, response.getStatus());
        verify(cartService).clearCart(10L);
    }

    @Test
    void startCheckout_newAddressProvided_addsAddressAndContinues() {
        CheckoutRequest request = buildRequest(null, null);
        request.setNewAddress(newAddressRequest());

        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(nonRxCartItem()));
        when(addressClient.addAddressForCurrentUser(any(UserAddressRequest.class), eq("token-abc")))
                .thenReturn(address());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(103L);
            return order;
        });

        OrderResponse response = checkoutService.startCheckout(10L, "token-abc", request);

        assertEquals(OrderStatus.PAYMENT_PENDING, response.getStatus());
        verify(addressClient).addAddressForCurrentUser(any(UserAddressRequest.class), eq("token-abc"));
    }

    @Test
    void startCheckout_missingAddressIdAndNewAddress_throwsBadRequest() {
        CheckoutRequest request = buildRequest(null, null);

        when(cartItemRepository.findByUserId(10L)).thenReturn(List.of(nonRxCartItem()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> checkoutService.startCheckout(10L, "token-abc", request));

        assertTrue(ex.getMessage().contains("Address ID or newAddress is required"));
    }

    private CheckoutRequest buildRequest(Long addressId, Long prescriptionId) {
        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(addressId);
        request.setDeliverySlot("9AM-12PM");
        request.setPrescriptionId(prescriptionId);
        return request;
    }

    private UserAddressResponse address() {
        return UserAddressResponse.builder()
                .id(1L)
                .streetAddress("FC Road")
                .city("Pune")
                .state("Maharashtra")
                .pincode(411001)
                .isDefault(true)
                .build();
    }

    private UserAddressRequest newAddressRequest() {
        UserAddressRequest request = new UserAddressRequest();
        request.setStreetAddress("JM Road");
        request.setCity("Pune");
        request.setState("Maharashtra");
        request.setPincode(411004);
        request.setIsDefault(Boolean.TRUE);
        return request;
    }

    private CartItem nonRxCartItem() {
        return CartItem.builder()
                .id(1L)
                .userId(10L)
                .medicineId(201L)
                .medicineName("Vitamin C")
                .unitPrice(new BigDecimal("50"))
                .quantity(1)
                .requiresPrescription(false)
                .build();
    }

    private CartItem rxCartItem() {
        return CartItem.builder()
                .id(2L)
                .userId(10L)
                .medicineId(202L)
                .medicineName("Antibiotic")
                .unitPrice(new BigDecimal("120"))
                .quantity(1)
                .requiresPrescription(true)
                .build();
    }
}
