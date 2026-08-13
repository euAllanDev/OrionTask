package com.oriontask.organization.adapter.out.persistence;

import com.oriontask.organization.domain.model.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organizations")
class OrganizationJpaEntity {
  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected OrganizationJpaEntity() {}

  OrganizationJpaEntity(Organization organization) {
    id = organization.id();
    name = organization.name();
    createdAt = organization.createdAt();
    updatedAt = organization.updatedAt();
  }

  Organization toDomain() {
    return new Organization(id, name, createdAt, updatedAt);
  }
}
