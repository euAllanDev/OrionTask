package com.oriontask.identity.application.port.in;

public interface LogoutCurrentSessionUseCase {
  void logout(String sessionToken);
}
