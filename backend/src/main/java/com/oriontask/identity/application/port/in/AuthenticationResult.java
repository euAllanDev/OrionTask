package com.oriontask.identity.application.port.in;

import java.util.UUID;

public record AuthenticationResult(Status status, String sessionToken, UUID accountId) {
  public enum Status {
    AUTHENTICATED,
    INVALID_CREDENTIALS,
    RATE_LIMITED
  }

  public static AuthenticationResult authenticated(String sessionToken, UUID accountId) {
    return new AuthenticationResult(Status.AUTHENTICATED, sessionToken, accountId);
  }

  public static AuthenticationResult invalidCredentials() {
    return new AuthenticationResult(Status.INVALID_CREDENTIALS, null, null);
  }

  public static AuthenticationResult rateLimited() {
    return new AuthenticationResult(Status.RATE_LIMITED, null, null);
  }
}
