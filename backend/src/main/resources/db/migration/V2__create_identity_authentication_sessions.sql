CREATE TABLE identity_authentication_sessions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES identity_accounts(id),
    token_derivation VARCHAR(128) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    last_activity_at TIMESTAMPTZ NOT NULL,
    absolute_expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL
);

CREATE INDEX idx_identity_authentication_sessions_account_id
    ON identity_authentication_sessions(account_id);
CREATE INDEX idx_identity_authentication_sessions_expiration
    ON identity_authentication_sessions(absolute_expires_at, last_activity_at);
