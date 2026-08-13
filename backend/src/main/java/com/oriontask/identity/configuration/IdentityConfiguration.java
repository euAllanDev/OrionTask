package com.oriontask.identity.configuration;

import com.oriontask.identity.application.port.in.AuthenticateAccountUseCase;
import com.oriontask.identity.application.port.in.GetCurrentSessionIdentityUseCase;
import com.oriontask.identity.application.port.in.LogoutCurrentSessionUseCase;
import com.oriontask.identity.application.port.in.RegisterAccountUseCase;
import com.oriontask.identity.application.port.out.AccountRegistrationAudit;
import com.oriontask.identity.application.port.out.AccountStore;
import com.oriontask.identity.application.port.out.AuthenticationAudit;
import com.oriontask.identity.application.port.out.AuthenticationRateLimiter;
import com.oriontask.identity.application.port.out.AuthenticationSessionStore;
import com.oriontask.identity.application.port.out.PasswordHasher;
import com.oriontask.identity.application.port.out.RegistrationRateLimiter;
import com.oriontask.identity.application.port.out.SessionTokenGenerator;
import com.oriontask.identity.application.port.out.TokenDeriver;
import com.oriontask.identity.application.usecase.AuthenticateAccountService;
import com.oriontask.identity.application.usecase.GetCurrentSessionIdentityService;
import com.oriontask.identity.application.usecase.LogoutCurrentSessionService;
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

  @Bean
  AuthenticateAccountUseCase authenticateAccountUseCase(
      AccountStore accountStore,
      PasswordHasher passwordHasher,
      AuthenticationSessionStore sessionStore,
      SessionTokenGenerator tokenGenerator,
      TokenDeriver tokenDeriver,
      AuthenticationRateLimiter rateLimiter,
      AuthenticationAudit audit,
      Clock clock) {
    return new AuthenticateAccountService(
        accountStore,
        passwordHasher,
        sessionStore,
        tokenGenerator,
        tokenDeriver,
        rateLimiter,
        audit,
        clock);
  }

  @Bean
  LogoutCurrentSessionUseCase logoutCurrentSessionUseCase(
      AuthenticationSessionStore sessionStore,
      TokenDeriver tokenDeriver,
      AuthenticationAudit audit,
      Clock clock) {
    return new LogoutCurrentSessionService(sessionStore, tokenDeriver, audit, clock);
  }

  @Bean
  GetCurrentSessionIdentityUseCase getCurrentSessionIdentityUseCase(AccountStore accountStore) {
    return new GetCurrentSessionIdentityService(accountStore);
  }
}
