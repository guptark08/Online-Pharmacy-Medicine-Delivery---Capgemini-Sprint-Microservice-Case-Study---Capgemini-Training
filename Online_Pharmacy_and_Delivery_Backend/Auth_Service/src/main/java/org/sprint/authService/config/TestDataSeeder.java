package org.sprint.authService.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.authService.dao.AddressRepository;
import org.sprint.authService.dao.UserRepository;
import org.sprint.authService.entities.Address;
import org.sprint.authService.entities.User;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class TestDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        User admin = ensureUser("admin", "System Admin", "admin@pharmacy.local", "9999900000", "Admin@123", "ADMIN");
        User alice = ensureUser("alice", "Alice Sharma", "alice@pharmacy.local", "9999900001", "Alice@123", "CUSTOMER");
        User bob = ensureUser("bob", "Bob Verma", "bob@pharmacy.local", "9999900002", "Bob@12345", "CUSTOMER");

        ensureAddress(alice, "221 MG Road", "Bengaluru", "Karnataka", 560001, true);
        ensureAddress(alice, "14 Lake View", "Bengaluru", "Karnataka", 560034, false);
        ensureAddress(bob, "88 Park Street", "Kolkata", "West Bengal", 700016, true);
        ensureAddress(admin, "1 Admin Block", "Bengaluru", "Karnataka", 560048, true);
    }

    private User ensureUser(
            String username,
            String name,
            String email,
            String mobile,
            String rawPassword,
            String role) {

        return userRepository.findAll().stream()
                .filter(user -> username.equalsIgnoreCase(user.getUsername()))
                .findFirst()
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(username)
                        .name(name)
                        .email(email)
                        .mobile(mobile)
                        .password(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .status(true)
                        .build()));
    }

    private void ensureAddress(
            User user,
            String street,
            String city,
            String state,
            int pincode,
            boolean isDefault) {

        List<Address> existingAddresses = addressRepository.findByUserId(user.getId());
        boolean alreadyPresent = existingAddresses.stream()
                .anyMatch(address -> street.equalsIgnoreCase(address.getStreet_address())
                        && city.equalsIgnoreCase(address.getCity())
                        && state.equalsIgnoreCase(address.getState())
                        && pincode == address.getPincode());

        if (alreadyPresent) {
            return;
        }

        addressRepository.save(Address.builder()
                .user(user)
                .street_address(street)
                .city(city)
                .state(state)
                .pincode(pincode)
                .isDefault(isDefault)
                .build());
    }
}
