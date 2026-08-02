package com.oriontask.identity.configuration;

import com.oriontask.identity.application.port.in.RegisterAccountUseCase;
import com.oriontask.identity.application.port.out.AccountRegistrationAudit;
import com.oriontask.identity.application.port.out.AccountStore;
import com.oriontask.identity.application.port.out.PasswordHasher;
import com.oriontask.identity.application.port.out.RegistrationRateLimiter;
import com.oriontask.identity.application.usecase.RegisterAccountService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class IdentityConfiguration {
  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  RegisterAccountUseCase registerAccountUseCase(
      AccountStore accountStore,
      PasswordHasher passwordHasher,
      RegistrationRateLimiter rateLimiter,
      AccountRegistrationAudit audit,
      Clock clock) {
    return new RegisterAccountService(accountStore, passwordHasher, rateLimiter, audit, clock);
  }
}
