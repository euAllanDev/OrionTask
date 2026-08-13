package com.oriontask.ticket.application.usecase;

import com.oriontask.ticket.application.port.in.TicketReadingUseCase;
import com.oriontask.ticket.application.port.out.TicketReadingStore;
import com.oriontask.ticket.application.query.TicketListQuery;
import com.oriontask.ticket.application.query.TicketPage;
import com.oriontask.ticket.domain.model.Ticket;
import java.util.Optional;
import java.util.UUID;

public class TicketReadingService implements TicketReadingUseCase {
  private final TicketReadingStore store;

  public TicketReadingService(TicketReadingStore store) {
    this.store = store;
  }

  @Override
  public Optional<Ticket> get(UUID organizationId, UUID ticketId, UUID accountId) {
    if (organizationId == null || ticketId == null || accountId == null) {
      return Optional.empty();
    }
    return store.find(organizationId, ticketId, accountId);
  }

  @Override
  public Optional<TicketPage> list(TicketListQuery query) {
    if (query == null
        || query.organizationId() == null
        || query.accountId() == null
        || query.page() < 0
        || query.size() < 1
        || query.size() > 100) {
      throw new IllegalArgumentException("Invalid ticket list query");
    }
    return store.list(query);
  }
}
