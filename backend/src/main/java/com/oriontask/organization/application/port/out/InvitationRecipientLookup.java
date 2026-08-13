package com.oriontask.organization.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface InvitationRecipientLookup {
  Optional<UUID> findAccountIdByNormalizedEmail(String normalizedEmail);
}
