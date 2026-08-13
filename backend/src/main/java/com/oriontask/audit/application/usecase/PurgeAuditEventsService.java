package com.oriontask.audit.application.usecase;

import com.oriontask.audit.application.port.out.AuditEventStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;

public class PurgeAuditEventsService {
  private final AuditEventStore store;
  private final Clock clock;

  public PurgeAuditEventsService(AuditEventStore store, Clock clock) {
    this.store = store;
    this.clock = clock;
  }

  public void purge() {
    Instant cutoff = ZonedDateTime.now(clock).minusMonths(12).toInstant();
    store.deleteOccurredAtOrBefore(cutoff);
  }
}
