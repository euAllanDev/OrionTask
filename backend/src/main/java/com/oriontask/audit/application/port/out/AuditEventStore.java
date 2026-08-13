package com.oriontask.audit.application.port.out;

import com.oriontask.audit.domain.model.AuditEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditEventStore {
  void save(AuditEvent event);

  AuthorizationResult findAuthorized(UUID organizationId, UUID accountId, int offset, int size);

  void deleteOccurredAtOrBefore(Instant cutoff);

  record AuthorizationResult(Access access, List<AuditEvent> events) {
    public enum Access {
      ALLOWED,
      FORBIDDEN,
      NOT_FOUND
    }
  }
}
