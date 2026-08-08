CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE organization_memberships (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    account_id UUID NOT NULL REFERENCES identity_accounts(id),
    role VARCHAR(32) NOT NULL CHECK (role = 'OWNER'),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_organization_memberships_account_organization UNIQUE (account_id, organization_id)
);

CREATE INDEX idx_organization_memberships_organization_id
    ON organization_memberships(organization_id);
