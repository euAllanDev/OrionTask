create table organization_tickets (
    id uuid primary key,
    organization_id uuid not null references organizations (id),
    customer_id uuid not null references organization_clients (id),
    creator_account_id uuid not null references identity_accounts (id),
    assignee_account_id uuid references identity_accounts (id),
    title varchar(120) not null,
    description varchar(4000),
    priority varchar(16) not null check (priority in ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status varchar(16) not null check (status = 'OPEN'),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_organization_tickets_organization_id on organization_tickets (organization_id);
create index idx_organization_tickets_customer_id on organization_tickets (customer_id);
create index idx_organization_tickets_assignee_account_id on organization_tickets (assignee_account_id);
