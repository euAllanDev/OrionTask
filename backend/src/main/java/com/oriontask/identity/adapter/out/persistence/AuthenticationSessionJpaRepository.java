package com.oriontask.identity.adapter.out.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

interface AuthenticationSessionJpaRepository
    extends JpaRepository<AuthenticationSessionJpaEntity, UUID> {
  Optional<AuthenticationSessionJpaEntity> findByTokenDerivation(String tokenDerivation);

  @Modifying
  @Transactional
  @Query(
      "update AuthenticationSessionJpaEntity session set session.lastActivityAt = :now "
          + "where session.id = :id and session.revokedAt is null "
          + "and session.absoluteExpiresAt > :now and session.lastActivityAt <= :threshold "
          + "and session.lastActivityAt > :inactivityCutoff")
  void updateLastActivityIfNeeded(
      @Param("id") UUID id,
      @Param("now") Instant now,
      @Param("threshold") Instant threshold,
      @Param("inactivityCutoff") Instant inactivityCutoff);

  @Modifying
  @Transactional
  @Query(
      "update AuthenticationSessionJpaEntity session set session.revokedAt = :now "
          + "where session.tokenDerivation = :tokenDerivation and session.revokedAt is null")
  void revokeByTokenDerivation(
      @Param("tokenDerivation") String tokenDerivation, @Param("now") Instant now);
}
