package com.oriontask.organization.application.port.in;

public interface RevokeMembershipUseCase {
  void revoke(RevokeMembershipCommand command);
}
