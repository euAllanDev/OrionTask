package com.oriontask.identity.adapter.out.ratelimit;

import com.oriontask.identity.application.port.out.AuthenticationRateLimiter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
class InMemoryAuthenticationRateLimiter implements AuthenticationRateLimiter {
  private static final Duration WINDOW = Duration.ofMinutes(15);
  private static final int IDENTIFIER_LIMIT = 5;
  private static final int ORIGIN_LIMIT = 20;
  private final Clock clock;
  private final Map<String, Deque<Instant>> attemptsByOrigin = new ConcurrentHashMap<>();
  private final Map<String, IdentifierFailures> failuresByIdentifier = new ConcurrentHashMap<>();

  InMemoryAuthenticationRateLimiter(Clock clock) {
    this.clock = clock;
  }

  @Override
  public synchronized RateLimitDecision allowAttempt(String sourceIp, String protectedIdentifier) {
    Instant now = Instant.now(clock);
    Deque<Instant> originAttempts =
        attemptsByOrigin.computeIfAbsent(sourceIp, ignored -> new ArrayDeque<>());
    removeExpired(originAttempts, now);
    if (originAttempts.size() >= ORIGIN_LIMIT) {
      return RateLimitDecision.ORIGIN_BLOCKED;
    }
    originAttempts.addLast(now);

    IdentifierFailures failures = failuresByIdentifier.get(protectedIdentifier);
    if (failures != null
        && failures.nextAllowedAt != null
        && now.isBefore(failures.nextAllowedAt)) {
      return RateLimitDecision.IDENTIFIER_BLOCKED;
    }
    return RateLimitDecision.ALLOWED;
  }

  @Override
  public synchronized void recordFailure(String protectedIdentifier) {
    Instant now = Instant.now(clock);
    IdentifierFailures failures =
        failuresByIdentifier.computeIfAbsent(
            protectedIdentifier, ignored -> new IdentifierFailures());
    removeExpired(failures.failures, now);
    failures.failures.addLast(now);
    if (failures.failures.size() >= IDENTIFIER_LIMIT) {
      failures.nextAllowedAt = now.plus(WINDOW);
    } else {
      // Enforce the progressive delay without blocking a servlet thread.
      failures.nextAllowedAt = now.plusSeconds(1L << (failures.failures.size() - 1));
    }
  }

  @Override
  public synchronized void recordSuccess(String protectedIdentifier) {
    failuresByIdentifier.remove(protectedIdentifier);
  }

  private static void removeExpired(Deque<Instant> attempts, Instant now) {
    Instant threshold = now.minus(WINDOW);
    while (!attempts.isEmpty() && !attempts.peekFirst().isAfter(threshold)) {
      attempts.removeFirst();
    }
  }

  private static final class IdentifierFailures {
    private final Deque<Instant> failures = new ArrayDeque<>();
    private Instant nextAllowedAt;
  }
}
