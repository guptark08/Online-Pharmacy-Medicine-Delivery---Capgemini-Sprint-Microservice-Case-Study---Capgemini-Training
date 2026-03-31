package org.sprint.authService.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sprint.authService.entities.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoked = true WHERE t.user.id = :userId AND t.revoked = false")
    int revokeAllActiveTokensByUserId(@Param("userId") Long userId);
}
