package com.oriontask.audit.domain.model;

public enum AuditAction {
  ORGANIZATION_CREATED("organization.created"),
  MEMBERSHIP_INVITATION_CREATED("membership_invitation.created"),
  MEMBERSHIP_INVITATION_ACCEPTED("membership_invitation.accepted"),
  ORGANIZATION_MEMBERSHIP_REVOKED("organization.membership_revoked"),
  ORGANIZATION_CLIENT_CREATED("organization.client_created"),
  ORGANIZATION_CLIENT_UPDATED("organization.client_updated"),
  ORGANIZATION_CLIENT_DEACTIVATED("organization.client_deactivated");

  private final String value;

  AuditAction(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
