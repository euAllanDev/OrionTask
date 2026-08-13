CREATE TABLE identity_accounts (
    id UUID PRIMARY KEY,
    normalized_email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
