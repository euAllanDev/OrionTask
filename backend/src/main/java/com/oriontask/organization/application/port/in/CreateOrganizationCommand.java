package com.oriontask.organization.application.port.in;

import java.util.UUID;

public record CreateOrganizationCommand(UUID accountId, String name) {}
