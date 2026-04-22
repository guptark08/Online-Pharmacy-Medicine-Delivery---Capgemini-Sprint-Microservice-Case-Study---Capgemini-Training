package org.sprint.authService.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.sprint.authService.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndStatus(String username, boolean status);

    Optional<User> findByEmailAndStatus(String email, boolean status);

    Optional<User> findByUsernameIgnoreCaseAndStatus(String username, boolean status);

    Optional<User> findByEmailIgnoreCaseAndStatus(String email, boolean status);

    @Query("SELECT u FROM User u WHERE (LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)) AND u.status = :status")
    Optional<User> findByUsernameOrEmailIgnoreCaseAndStatus(String identifier, boolean status);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.addresses WHERE u.username = :username AND u.status = :status")
    Optional<User> findByUsernameIgnoreCaseAndStatusWithAddresses(String username, boolean status);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByMobile(String mobile);
}
