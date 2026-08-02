package com.oriontask.identity.adapter.out.ratelimit;

import com.oriontask.identity.application.port.out.RegistrationRateLimiter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
class InMemoryRegistrationRateLimiter implements RegistrationRateLimiter {
  private static final Duration WINDOW = Duration.ofMinutes(15);
  private final ConcurrentHashMap<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

  @Override
  public boolean allow(String sourceIp, String normalizedEmail) {
    Instant now = Instant.now();
    return allow("ip:" + sourceIp, 5, now) && allow("email:" + normalizedEmail, 3, now);
  }

  private boolean allow(String key, int limit, Instant now) {
    Deque<Instant> window = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
    synchronized (window) {
      Instant threshold = now.minus(WINDOW);
      while (!window.isEmpty() && !window.peekFirst().isAfter(threshold)) {
        window.removeFirst();
      }
      if (window.size() >= limit) {
        return false;
      }
      window.addLast(now);
      return true;
    }
  }
}
