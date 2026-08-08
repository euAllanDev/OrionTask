CREATE TABLE membership_invitations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    recipient_account_id UUID NOT NULL REFERENCES identity_accounts(id),
    role VARCHAR(32) NOT NULL CHECK (role IN ('ADMIN', 'TECHNICIAN')),
    token_derivation VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED')),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    CHECK (
        (status = 'ACCEPTED' AND accepted_at IS NOT NULL)
        OR (status IN ('PENDING', 'EXPIRED') AND accepted_at IS NULL)
    )
);

CREATE UNIQUE INDEX uq_membership_invitations_pending_recipient
    ON membership_invitations (organization_id, recipient_account_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_membership_invitations_token_derivation
    ON membership_invitations (token_derivation);
