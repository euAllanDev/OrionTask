package com.oriontask.client.configuration;

import com.oriontask.client.application.port.in.ClientManagementUseCase;
import com.oriontask.client.application.port.out.ClientAudit;
import com.oriontask.client.application.port.out.ClientStore;
import com.oriontask.client.application.usecase.ClientManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ClientConfiguration {
  @Bean
  ClientManagementUseCase clientManagementUseCase(ClientStore store, ClientAudit audit) {
    return new ClientManagementService(store, audit);
  }
}
