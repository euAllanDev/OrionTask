package com.oriontask.identity.application.port.in;

public interface AuthenticateAccountUseCase {
  AuthenticationResult authenticate(AuthenticationCommand command);
}
