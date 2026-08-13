package com.oriontask.ticket.application.port.out;

import com.oriontask.ticket.application.command.OpenTicketCommand;
import com.oriontask.ticket.domain.model.Ticket;
import java.util.Optional;

public interface TicketOpeningStore {
  Optional<Ticket> open(OpenTicketCommand command, java.util.UUID ticketId);
}
