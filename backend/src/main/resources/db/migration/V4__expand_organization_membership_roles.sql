ALTER TABLE organization_memberships
    DROP CONSTRAINT organization_memberships_role_check;

ALTER TABLE organization_memberships
    ADD CONSTRAINT organization_memberships_role_check
    CHECK (role IN ('OWNER', 'ADMIN', 'TECHNICIAN'));
