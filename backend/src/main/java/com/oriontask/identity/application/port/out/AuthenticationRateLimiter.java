package com.oriontask.identity.application.port.out;

public interface AuthenticationRateLimiter {
  RateLimitDecision allowAttempt(String sourceIp, String protectedIdentifier);

  void recordFailure(String protectedIdentifier);

  void recordSuccess(String protectedIdentifier);

  enum RateLimitDecision {
    ALLOWED,
    IDENTIFIER_BLOCKED,
    ORIGIN_BLOCKED
  }
}
