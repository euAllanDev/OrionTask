package com.oriontask.identity.application.port.in;

import java.util.UUID;

public record CurrentSessionIdentity(UUID accountId, String email) {}
