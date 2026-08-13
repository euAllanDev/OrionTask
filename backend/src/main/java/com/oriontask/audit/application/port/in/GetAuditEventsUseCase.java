package com.oriontask.audit.application.port.in;

import com.oriontask.audit.domain.model.AuditEvent;
import java.util.List;
import java.util.UUID;

public interface GetAuditEventsUseCase {
  Result get(UUID organizationId, UUID accountId, int page, int size);

  record Result(Status status, List<AuditEvent> events) {
    public enum Status {
      ALLOWED,
      FORBIDDEN,
      NOT_FOUND
    }
  }
}
