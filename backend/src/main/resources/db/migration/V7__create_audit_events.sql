create table audit_events (
    id uuid primary key,
    organization_id uuid not null,
    actor_account_id uuid not null,
    action varchar(64) not null check (action in (
        'organization.created',
        'membership_invitation.created',
        'membership_invitation.accepted',
        'organization.membership_revoked',
        'organization.client_created',
        'organization.client_updated',
        'organization.client_deactivated'
    )),
    resource_type varchar(32) not null,
    resource_id uuid not null,
    occurred_at timestamptz not null
);

create index idx_audit_events_organization_occurred_id
    on audit_events (organization_id, occurred_at desc, id desc);
