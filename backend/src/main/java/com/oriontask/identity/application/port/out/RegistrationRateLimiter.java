package com.oriontask.identity.application.port.out;

public interface RegistrationRateLimiter {
  boolean allow(String sourceIp, String normalizedEmail);
}
