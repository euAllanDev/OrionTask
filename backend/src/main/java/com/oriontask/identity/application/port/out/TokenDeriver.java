package com.oriontask.identity.application.port.out;

public interface TokenDeriver {
  String derive(String value);
}
