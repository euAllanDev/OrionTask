package com.oriontask.ticket.configuration;

import com.oriontask.ticket.application.port.in.OpenTicketUseCase;
import com.oriontask.ticket.application.port.in.TicketReadingUseCase;
import com.oriontask.ticket.application.port.out.TicketOpeningStore;
import com.oriontask.ticket.application.port.out.TicketReadingStore;
import com.oriontask.ticket.application.usecase.OpenTicketService;
import com.oriontask.ticket.application.usecase.TicketReadingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TicketConfiguration {
  @Bean
  OpenTicketUseCase openTicketUseCase(TicketOpeningStore store) {
    return new OpenTicketService(store);
  }

  @Bean
  TicketReadingUseCase ticketReadingUseCase(TicketReadingStore store) {
    return new TicketReadingService(store);
  }
}
