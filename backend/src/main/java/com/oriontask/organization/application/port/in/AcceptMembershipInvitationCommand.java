package com.oriontask.organization.application.port.in;

import java.util.UUID;

public record AcceptMembershipInvitationCommand(String token, UUID accountId) {}
