package com.oriontask.organization.application.port.in;

import java.time.Instant;
import java.util.UUID;

public record CreateOrganizationResult(UUID id, String name, Instant createdAt) {}
