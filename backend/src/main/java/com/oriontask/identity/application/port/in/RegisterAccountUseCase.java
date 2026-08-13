package com.oriontask.identity.application.port.in;

public interface RegisterAccountUseCase {
  RegistrationResult register(RegistrationCommand command);
}
