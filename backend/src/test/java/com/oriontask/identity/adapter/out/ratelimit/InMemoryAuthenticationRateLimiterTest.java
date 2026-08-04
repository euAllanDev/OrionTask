package com.oriontask.identity.adapter.out.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.out.AuthenticationRateLimiter.RateLimitDecision;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class InMemoryAuthenticationRateLimiterTest {
  @Test
  void blocksIdentifierAfterFiveFailuresWithoutBlockingThreads() {
    MutableClock clock = new MutableClock(Instant.parse("2026-08-04T12:00:00Z"));
    InMemoryAuthenticationRateLimiter limiter = new InMemoryAuthenticationRateLimiter(clock);

    for (int index = 0; index < 5; index++) {
      assertThat(limiter.allowAttempt("127.0.0.1", "protected-id"))
          .isEqualTo(RateLimitDecision.ALLOWED);
      limiter.recordFailure("protected-id");
      clock.advanceSeconds(16);
    }

    assertThat(limiter.allowAttempt("127.0.0.1", "protected-id"))
        .isEqualTo(RateLimitDecision.IDENTIFIER_BLOCKED);
  }

  @Test
  void blocksOriginAfterTwentyAttempts() {
    MutableClock clock = new MutableClock(Instant.parse("2026-08-04T12:00:00Z"));
    InMemoryAuthenticationRateLimiter limiter = new InMemoryAuthenticationRateLimiter(clock);

    for (int index = 0; index < 20; index++) {
      assertThat(limiter.allowAttempt("127.0.0.1", "protected-" + index))
          .isEqualTo(RateLimitDecision.ALLOWED);
    }

    assertThat(limiter.allowAttempt("127.0.0.1", "protected-extra"))
        .isEqualTo(RateLimitDecision.ORIGIN_BLOCKED);
  }

  private static final class MutableClock extends Clock {
    private Instant instant;

    private MutableClock(Instant instant) {
      this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
      return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }

    private void advanceSeconds(long seconds) {
      instant = instant.plusSeconds(seconds);
    }
  }
}
