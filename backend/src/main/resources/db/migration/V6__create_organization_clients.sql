create table organization_clients (
    id uuid primary key,
    organization_id uuid not null references organizations (id),
    name varchar(120) not null,
    status varchar(16) not null check (status in ('ACTIVE', 'INACTIVE')),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    deactivated_at timestamptz
);

create index idx_organization_clients_status_created_at
    on organization_clients (organization_id, status, created_at desc);
