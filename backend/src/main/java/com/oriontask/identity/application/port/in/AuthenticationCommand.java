package com.oriontask.identity.application.port.in;

public record AuthenticationCommand(String email, String password, String sourceIp) {}
