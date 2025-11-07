package com.tamm.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tamm.identity.entity.ValidatedRefreshToken;

@Repository
public interface ValidatedRefreshTokenRepository extends JpaRepository<ValidatedRefreshToken, String> {

    /**
     * Find refresh token by jti (JWT ID)
     */
    Optional<ValidatedRefreshToken> findByJti(String jti);

    /**
     * Find refresh token by user ID
     */
    Optional<ValidatedRefreshToken> findByUserId(String userId);

    /**
     * Check if refresh token exists by jti
     */
    boolean existsByJti(String jti);

    /**
     * Delete all refresh tokens for a user
     * Used when user changes password or security reset
     */
    void deleteByUserId(String userId);
}
