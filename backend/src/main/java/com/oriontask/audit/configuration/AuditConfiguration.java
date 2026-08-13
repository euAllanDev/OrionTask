package com.oriontask.audit.configuration;

import com.oriontask.audit.application.port.in.GetAuditEventsUseCase;
import com.oriontask.audit.application.port.out.AuditEventStore;
import com.oriontask.audit.application.usecase.GetAuditEventsService;
import com.oriontask.audit.application.usecase.PurgeAuditEventsService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
class AuditConfiguration {
  @Bean
  GetAuditEventsUseCase getAuditEventsUseCase(AuditEventStore store) {
    return new GetAuditEventsService(store);
  }

  @Bean
  AuditRetentionJob auditRetentionJob(AuditEventStore store, Clock clock) {
    return new AuditRetentionJob(new PurgeAuditEventsService(store, clock));
  }

  static class AuditRetentionJob {
    private final PurgeAuditEventsService purgeService;

    AuditRetentionJob(PurgeAuditEventsService purgeService) {
      this.purgeService = purgeService;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void purgeExpiredEvents() {
      purgeService.purge();
    }
  }
}
