DROP TABLE IF EXISTS contact_persons;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP SEQUENCE IF EXISTS global_seq;

CREATE SEQUENCE global_seq START WITH 100000;

CREATE TABLE users
(
    id         BIGINT DEFAULT nextval('global_seq')  PRIMARY KEY,
    email      VARCHAR(128)                       NOT NULL,
    first_name VARCHAR(32)                        NOT NULL,
    last_name  VARCHAR(32)                        NOT NULL,
    password   VARCHAR(128)                       NOT NULL,
    enabled    BOOL                DEFAULT TRUE   NOT NULL,
    registered TIMESTAMP           DEFAULT now()  NOT NULL
);
CREATE UNIQUE INDEX users_unique_email_idx ON users (email);

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role    VARCHAR(9),
    CONSTRAINT user_roles_unique_idx UNIQUE (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens
(
    id          BIGINT      DEFAULT nextval('global_seq')  PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    token       VARCHAR(36) NOT NULL,
    expiry_date TIMESTAMP  NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX password_reset_tokens_unique_user_idx ON password_reset_tokens (user_id);

CREATE TABLE companies
(
    id       BIGINT DEFAULT nextval('global_seq')  PRIMARY KEY,
    name     VARCHAR(128)      NOT NULL,
    city     VARCHAR(32)       NOT NULL,
    street   VARCHAR(64)       NOT NULL,
    house    VARCHAR(32)       NOT NULL,
    zipcode  VARCHAR(6)        NOT NULL
);
CREATE UNIQUE INDEX companies_unique_name_idx ON companies (name);

CREATE TABLE contact_persons
(
    company_id  BIGINT           NOT NULL,
    position    VARCHAR(64)      NOT NULL,
    first_name  VARCHAR(32)      NOT NULL,
    middle_name VARCHAR(32)      NOT NULL,
    last_name   VARCHAR(32)      NOT NULL,
    phone       VARCHAR(32),
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX contact_persons_unique_company_position_idx ON contact_persons (company_id, position);
