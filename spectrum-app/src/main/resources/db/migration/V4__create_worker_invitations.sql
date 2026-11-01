CREATE TABLE worker_invitations (
    id UUID PRIMARY KEY,
    email VARCHAR NOT NULL,
    token_hash VARCHAR NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX worker_invitations_pending_email_idx
    ON worker_invitations (email)
    WHERE consumed_at IS NULL AND revoked_at IS NULL;
