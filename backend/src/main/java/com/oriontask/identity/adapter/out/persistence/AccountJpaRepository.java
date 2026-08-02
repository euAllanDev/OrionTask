package com.oriontask.identity.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
  boolean existsByNormalizedEmail(String normalizedEmail);
}
