package org.sprint.authService.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.sprint.authService.dao.AddressRepository;
import org.sprint.authService.dto.AddressRequest;
import org.sprint.authService.dto.AddressResponse;
import org.sprint.authService.entities.Address;
import org.sprint.authService.entities.User;
import org.sprint.authService.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addAddress_success_savesAndReturnsResponse() {
        authenticateAs("alice");
        User user = buildUser(10L, "alice");

        AddressRequest request = AddressRequest.builder()
                .street_address("FC Road")
                .city("Pune")
                .state("Maharashtra")
                .pincode(411001)
                .isDefault(false)
                .build();

        Address savedAddress = Address.builder()
                .id(1L)
                .user(user)
                .street_address("FC Road")
                .city("Pune")
                .state("Maharashtra")
                .pincode(411001)
                .isDefault(false)
                .build();

        when(userService.getActiveByUsername("alice")).thenReturn(user);
        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);

        AddressResponse response = addressService.addAddressForCurrentUser(request);

        assertEquals("Pune", response.getCity());
        assertEquals(411001, response.getPincode());
    }

    @Test
    void addAddress_withDefaultTrue_resetsExistingDefaults() {
        authenticateAs("alice");
        User user = buildUser(10L, "alice");

        Address existingDefault = Address.builder()
                .id(55L)
                .user(user)
                .street_address("Old street")
                .city("Pune")
                .state("Maharashtra")
                .pincode(411002)
                .isDefault(true)
                .build();

        AddressRequest request = AddressRequest.builder()
                .street_address("New street")
                .city("Mumbai")
                .state("Maharashtra")
                .pincode(400001)
                .isDefault(true)
                .build();

        Address savedNew = Address.builder()
                .id(99L)
                .user(user)
                .street_address("New street")
                .city("Mumbai")
                .state("Maharashtra")
                .pincode(400001)
                .isDefault(true)
                .build();

        when(userService.getActiveByUsername("alice")).thenReturn(user);
        when(addressRepository.findByUserIdAndIsDefaultTrue(10L)).thenReturn(List.of(existingDefault));
        when(addressRepository.save(any(Address.class))).thenReturn(savedNew);

        addressService.addAddressForCurrentUser(request);

        ArgumentCaptor<List<Address>> defaultsCaptor = ArgumentCaptor.forClass(List.class);
        verify(addressRepository).saveAll(defaultsCaptor.capture());
        assertFalse(defaultsCaptor.getValue().get(0).isDefault());
        assertFalse(existingDefault.isDefault());

        var inOrder = inOrder(addressRepository);
        inOrder.verify(addressRepository).saveAll(anyList());
        inOrder.verify(addressRepository).save(any(Address.class));
    }

    @Test
    void addAddress_withDefaultTrue_andNoExistingDefaults_skipsBulkUpdate() {
        authenticateAs("alice");
        User user = buildUser(10L, "alice");

        AddressRequest request = AddressRequest.builder()
                .street_address("New street")
                .city("Mumbai")
                .state("Maharashtra")
                .pincode(400001)
                .isDefault(true)
                .build();

        Address savedNew = Address.builder()
                .id(99L)
                .user(user)
                .street_address("New street")
                .city("Mumbai")
                .state("Maharashtra")
                .pincode(400001)
                .isDefault(true)
                .build();

        when(userService.getActiveByUsername("alice")).thenReturn(user);
        when(addressRepository.findByUserIdAndIsDefaultTrue(10L)).thenReturn(List.of());
        when(addressRepository.save(any(Address.class))).thenReturn(savedNew);

        addressService.addAddressForCurrentUser(request);

        verify(addressRepository, never()).saveAll(anyList());
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void updateAddress_addressNotOwnedByUser_throwsResourceNotFound() {
        authenticateAs("alice");
        User user = buildUser(10L, "alice");

        AddressRequest request = AddressRequest.builder()
                .street_address("FC Road")
                .city("Pune")
                .state("Maharashtra")
                .pincode(411001)
                .isDefault(false)
                .build();

        when(userService.getActiveByUsername("alice")).thenReturn(user);
        when(addressRepository.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressService.updateAddressForCurrentUser(99L, request));
    }

    @Test
    void updateAddress_success_updatesAndReturnsResponse() {
        authenticateAs("alice");
        User user = buildUser(10L, "alice");

        Address existingAddress = Address.builder()
                .id(7L)
                .user(user)
                .street_address("Old Road")
                .city("Pune")
                .state("Maharashtra")
                .pincode(411001)
                .isDefault(false)
                .build();

        AddressRequest request = AddressRequest.builder()
                .street_address("  New Road  ")
                .city("  Mumbai ")
                .state(" Maharashtra ")
                .pincode(400001)
                .isDefault(true)
                .build();

        Address oldDefault = Address.builder()
                .id(88L)
                .user(user)
                .street_address("Old default")
                .city("Pune")
                .state("Maharashtra")
                .pincode(411002)
                .isDefault(true)
                .build();

        Address savedUpdated = Address.builder()
                .id(7L)
                .user(user)
                .street_address("New Road")
                .city("Mumbai")
                .state("Maharashtra")
                .pincode(400001)
                .isDefault(true)
                .build();

        when(userService.getActiveByUsername("alice")).thenReturn(user);
        when(addressRepository.findByIdAndUserId(7L, 10L)).thenReturn(Optional.of(existingAddress));
        when(addressRepository.findByUserIdAndIsDefaultTrue(10L)).thenReturn(List.of(oldDefault));
        when(addressRepository.save(any(Address.class))).thenReturn(savedUpdated);

        AddressResponse response = addressService.updateAddressForCurrentUser(7L, request);

        assertEquals("Mumbai", response.getCity());
        assertEquals("New Road", response.getStreet_address());
        assertEquals(400001, response.getPincode());
        assertFalse(oldDefault.isDefault());
        verify(addressRepository).saveAll(anyList());
        verify(addressRepository).save(existingAddress);
    }

    @Test
    void deleteAddress_success_deletesFromRepository() {
        authenticateAs("alice");
        User user = buildUser(10L, "alice");
        Address ownedAddress = Address.builder()
                .id(7L)
                .user(user)
                .street_address("Baner")
                .city("Pune")
                .state("Maharashtra")
                .pincode(411045)
                .isDefault(false)
                .build();

        when(userService.getActiveByUsername("alice")).thenReturn(user);
        when(addressRepository.findByIdAndUserId(7L, 10L)).thenReturn(Optional.of(ownedAddress));

        addressService.deleteAddressForCurrentUser(7L);

        verify(addressRepository, times(1)).delete(ownedAddress);
    }

    @Test
    void deleteAddress_notFound_throwsResourceNotFound() {
        authenticateAs("alice");
        User user = buildUser(10L, "alice");
        when(userService.getActiveByUsername("alice")).thenReturn(user);
        when(addressRepository.findByIdAndUserId(123L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.deleteAddressForCurrentUser(123L));
    }

    @Test
    void getAllAddresses_withoutAuthentication_throwsAccessDenied() {
        SecurityContextHolder.clearContext();

        assertThrows(AccessDeniedException.class, () -> addressService.getAllAddressResponses());
    }

    @Test
    void getAllAddresses_withAnonymousUser_throwsAccessDenied() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("anonymousUser", "password", List.of()));
        SecurityContextHolder.setContext(context);

        assertThrows(AccessDeniedException.class, () -> addressService.getAllAddressResponses());
    }

    @Test
    void getAllAddresses_returnsOnlyCurrentUserAddresses() {
        authenticateAs("alice");
        User user = buildUser(10L, "alice");

        Address address1 = Address.builder()
                .id(1L)
                .user(user)
                .street_address("Street 1")
                .city("Pune")
                .state("Maharashtra")
                .pincode(411001)
                .isDefault(true)
                .build();

        Address address2 = Address.builder()
                .id(2L)
                .user(user)
                .street_address("Street 2")
                .city("Nashik")
                .state("Maharashtra")
                .pincode(422001)
                .isDefault(false)
                .build();

        when(userService.getActiveByUsername("alice")).thenReturn(user);
        when(addressRepository.findByUserId(10L)).thenReturn(List.of(address1, address2));

        List<AddressResponse> responses = addressService.getAllAddressResponses();

        assertEquals(2, responses.size());
        assertEquals("Pune", responses.get(0).getCity());
        assertEquals(411001, responses.get(0).getPincode());
        assertEquals("Nashik", responses.get(1).getCity());
        assertEquals(422001, responses.get(1).getPincode());
    }

    private void authenticateAs(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(username, "password", List.of()));
        SecurityContextHolder.setContext(context);
    }

    private User buildUser(Long id, String username) {
        return User.builder()
                .id(id)
                .name("Alice")
                .email("alice@example.com")
                .username(username)
                .mobile("9999999999")
                .password("encoded")
                .role("CUSTOMER")
                .status(true)
                .build();
    }
}
