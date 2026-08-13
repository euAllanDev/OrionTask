package com.oriontask.audit.domain.model;

import java.time.Instant;
import java.util.UUID;

public record AuditEvent(
    UUID id,
    UUID organizationId,
    UUID actorAccountId,
    AuditAction action,
    String resourceType,
    UUID resourceId,
    Instant occurredAt) {}
