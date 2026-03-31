package org.sprint.authService.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sprint.authService.entity.VerificationToken;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserIdAndTypeAndUsedFalse(Long userId, VerificationToken.VerificationType type);
}
