package com.oriontask.identity.application.port.out;

public interface PasswordHasher {
  String hash(String password);
}
