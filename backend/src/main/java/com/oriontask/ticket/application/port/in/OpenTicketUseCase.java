package com.oriontask.ticket.application.port.in;

import com.oriontask.ticket.application.command.OpenTicketCommand;
import com.oriontask.ticket.domain.model.Ticket;

public interface OpenTicketUseCase {
  Ticket open(OpenTicketCommand command);
}
