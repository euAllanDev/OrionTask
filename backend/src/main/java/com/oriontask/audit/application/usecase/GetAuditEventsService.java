package com.oriontask.audit.application.usecase;

import com.oriontask.audit.application.port.in.GetAuditEventsUseCase;
import com.oriontask.audit.application.port.out.AuditEventStore;
import java.util.UUID;

public class GetAuditEventsService implements GetAuditEventsUseCase {
  private final AuditEventStore store;

  public GetAuditEventsService(AuditEventStore store) {
    this.store = store;
  }

  @Override
  public Result get(UUID organizationId, UUID accountId, int page, int size) {
    AuditEventStore.AuthorizationResult result =
        store.findAuthorized(organizationId, accountId, Math.multiplyExact(page, size), size);
    return new Result(Result.Status.valueOf(result.access().name()), result.events());
  }
}
