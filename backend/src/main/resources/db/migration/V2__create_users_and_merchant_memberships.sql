CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash varchar(255) NOT NULL
);

CREATE UNIQUE INDEX users_uq_email_idx ON users (lower(email));

CREATE TABLE merchant_memberships (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    merchant_id BIGINT NOT NULL REFERENCES merchants(id),
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'STAFF')),

    CONSTRAINT uq_membership_users_and_merchant UNIQUE (user_id, merchant_id)
);
