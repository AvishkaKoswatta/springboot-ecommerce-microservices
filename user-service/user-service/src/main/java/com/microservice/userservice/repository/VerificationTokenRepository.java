package com.microservice.userservice.repository;

import com.microservice.userservice.entity.TokenType;
import com.microservice.userservice.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);

    Optional<VerificationToken> findByUserIdAndTokenType(Long userId, TokenType tokenType);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiresAt < :now AND vt.used = true")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.user.id = :userId AND vt.tokenType = :tokenType")
    void invalidateExistingTokens(@Param("userId") Long userId, @Param("tokenType") TokenType tokenType);
}
