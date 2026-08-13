package com.oriontask.ticket.application.port.in;

import com.oriontask.ticket.application.query.TicketListQuery;
import com.oriontask.ticket.application.query.TicketPage;
import com.oriontask.ticket.domain.model.Ticket;
import java.util.Optional;
import java.util.UUID;

public interface TicketReadingUseCase {
  Optional<Ticket> get(UUID organizationId, UUID ticketId, UUID accountId);

  Optional<TicketPage> list(TicketListQuery query);
}
