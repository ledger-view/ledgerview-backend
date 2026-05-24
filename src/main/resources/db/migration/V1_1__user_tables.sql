CREATE TABLE ledgerview.users
(
    id          uuid         NOT NULL DEFAULT gen_random_uuid(),
    email       VARCHAR(128) NOT NULL,
    external_id VARCHAR(256) NOT NULL,
    CONSTRAINT users_pk PRIMARY KEY (id),
    CONSTRAINT users_unique_email UNIQUE (email),
    CONSTRAINT users_unique_external_id UNIQUE (external_id)
);

CREATE UNIQUE INDEX users_unique_external_id_idx ON ledgerview.users (external_id);
