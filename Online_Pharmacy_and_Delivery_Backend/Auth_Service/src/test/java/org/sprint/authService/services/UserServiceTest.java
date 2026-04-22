package org.sprint.authService.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.sprint.authService.dao.UserRepository;
import org.sprint.authService.dto.UserRequest;
import org.sprint.authService.entities.User;
import org.sprint.authService.exception.DuplicateResourceException;
import org.sprint.authService.service.EmailEventPublisher;
import org.sprint.authService.service.VerificationService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private VerificationService verificationService;

    @Mock
    private EmailEventPublisher emailEventPublisher;

    @InjectMocks
    private UserService userService;

    private UserRequest request;

    @BeforeEach
    void setUp() {
        request = UserRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .username("testuser")
                .mobile("9999999999")
                .password("Password@123")
                .build();
    }

    @Test
    void addUser_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.addUser(request));
    }

    @Test
    void addUser_success_encodesPasswordAndSaves() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("testuser")).thenReturn(false);
        when(userRepository.existsByMobile("9999999999")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(verificationService.createEmailVerificationToken(1L)).thenReturn("verification-token");
        when(verificationService.getVerificationUrl("verification-token"))
                .thenReturn("http://localhost:5173/verify-email?token=verification-token");

        var response = userService.addUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertEquals("ENCODED", captor.getValue().getPassword());
        assertEquals(1L, response.getId());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals("9999999999", response.getMobile());
    }

    @Test
    void loadUserByUsername_assignsRoleAuthority() {
        User user = User.builder()
                .id(7L)
                .username("admin")
                .email("admin@x.com")
                .password("pwd")
                .role("ROLE_ADMIN")
                .status(true)
                .build();

        when(userRepository.findByUsernameIgnoreCaseAndStatus("admin", true)).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("admin");

        assertEquals("admin", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
