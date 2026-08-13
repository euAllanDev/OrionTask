package com.oriontask.identity.application.port.in;

public record RegistrationCommand(String email, String password, String sourceIp) {}
