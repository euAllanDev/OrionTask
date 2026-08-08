package com.oriontask.organization.application.port.out;

public interface InvitationTokenDeriver {
  String derive(String token);
}
