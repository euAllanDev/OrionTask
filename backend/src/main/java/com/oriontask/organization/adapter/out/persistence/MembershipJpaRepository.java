package com.oriontask.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MembershipJpaRepository extends JpaRepository<MembershipJpaEntity, UUID> {}
