CREATE TABLE ledgerview.accounts
(
    id          uuid           NOT NULL DEFAULT gen_random_uuid(),
    user_id     uuid           NOT NULL,
    name        VARCHAR(128)   NOT NULL,
    institution VARCHAR(128)   NOT NULL,
    type        VARCHAR(32)    NOT NULL,
    currency    VARCHAR(8)     NOT NULL,
    balance     NUMERIC(19, 4) NOT NULL DEFAULT 0,
    number      VARCHAR(64),
    CONSTRAINT accounts_pk PRIMARY KEY (id),
    CONSTRAINT accounts_user_fk FOREIGN KEY (user_id) REFERENCES ledgerview.users (id) ON DELETE CASCADE
);

CREATE INDEX accounts_user_idx ON ledgerview.accounts (user_id);

CREATE TABLE ledgerview.categories
(
    id      uuid         NOT NULL DEFAULT gen_random_uuid(),
    user_id uuid         NOT NULL,
    name    VARCHAR(64)  NOT NULL,
    color   VARCHAR(16)  NOT NULL,
    type    VARCHAR(16)  NOT NULL,
    CONSTRAINT categories_pk PRIMARY KEY (id),
    CONSTRAINT categories_user_fk FOREIGN KEY (user_id) REFERENCES ledgerview.users (id) ON DELETE CASCADE
);

CREATE INDEX categories_user_idx ON ledgerview.categories (user_id);

CREATE TABLE ledgerview.transactions
(
    id          uuid           NOT NULL DEFAULT gen_random_uuid(),
    user_id     uuid           NOT NULL,
    account_id  uuid           NOT NULL,
    category_id uuid           NOT NULL,
    title       VARCHAR(256)   NOT NULL,
    amount      NUMERIC(19, 4) NOT NULL,
    type        VARCHAR(16)    NOT NULL,
    date        DATE           NOT NULL,
    note        VARCHAR(512),
    CONSTRAINT transactions_pk PRIMARY KEY (id),
    CONSTRAINT transactions_user_fk FOREIGN KEY (user_id) REFERENCES ledgerview.users (id) ON DELETE CASCADE,
    CONSTRAINT transactions_account_fk FOREIGN KEY (account_id) REFERENCES ledgerview.accounts (id) ON DELETE CASCADE,
    CONSTRAINT transactions_category_fk FOREIGN KEY (category_id) REFERENCES ledgerview.categories (id) ON DELETE RESTRICT
);

CREATE INDEX transactions_user_date_idx ON ledgerview.transactions (user_id, date DESC);
CREATE INDEX transactions_account_idx ON ledgerview.transactions (account_id);
CREATE INDEX transactions_category_idx ON ledgerview.transactions (category_id);
