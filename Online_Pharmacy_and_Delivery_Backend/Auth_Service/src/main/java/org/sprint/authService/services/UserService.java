package org.sprint.authService.services;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.authService.dao.UserRepository;
import org.sprint.authService.dto.UserRequest;
import org.sprint.authService.dto.UserResponse;
import org.sprint.authService.entities.User;
import org.sprint.authService.exception.DuplicateResourceException;
import org.sprint.authService.service.EmailEventPublisher;
import org.sprint.authService.service.VerificationService;
import org.sprint.authService.dto.EmailVerificationEvent;
import org.sprint.authService.util.RoleNormalizer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final EmailEventPublisher emailEventPublisher;

    public User getActiveUserByIdentifier(String identifier) {
        return userRepository.findByUsernameIgnoreCaseAndStatus(identifier, true)
                .or(() -> userRepository.findByEmailIgnoreCaseAndStatus(identifier, true))
                .orElseThrow(() -> new UsernameNotFoundException("Active user not found: " + identifier));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = getActiveUserByIdentifier(identifier);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(RoleNormalizer.toAuthority(user.getRole()))))
                .build();
    }

    @Transactional
    public UserResponse addUser(UserRequest userRequest) {
        validateUniqueness(userRequest);

        User user = User.builder()
                .name(userRequest.getName().trim())
                .email(userRequest.getEmail().trim().toLowerCase())
                .username(userRequest.getUsername().trim())
                .mobile(userRequest.getMobile().trim())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .role("CUSTOMER")
                .status(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        String token = verificationService.createEmailVerificationToken(savedUser.getId());
        String verificationUrl = verificationService.getVerificationUrl(token);

        EmailVerificationEvent event = EmailVerificationEvent.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .userName(savedUser.getName())
                .verificationToken(token)
                .verificationUrl(verificationUrl)
                .build();

        emailEventPublisher.publishEmailVerificationEvent(event);

        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToUserResponse).toList();
    }

    @Transactional(readOnly = true)
    public User getActiveByUsername(String username) {
        return userRepository.findByUsernameIgnoreCaseAndStatusWithAddresses(username, true)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private void validateUniqueness(UserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername().trim())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByMobile(request.getMobile().trim())) {
            throw new DuplicateResourceException("Mobile number already registered");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .mobile(user.getMobile())
                .role(RoleNormalizer.normalizeOrDefault(user.getRole()))
                .status(user.isStatus())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}
