DELETE FROM user_roles;
DELETE FROM users;
ALTER SEQUENCE global_seq RESTART WITH 100000;

INSERT INTO users (email, first_name, last_name, password, enabled)
VALUES ('user@gmail.com','John', 'Smith', '{noop}password', true),
       ('admin@gmail.com','Viktor', 'Wran', '{noop}admin', true),
       ('archivist@gmail.com','Jack', 'London', '{noop}password', true),
       ('userDisabled@gmail.com','John', 'Doe', '{noop}password', false);

INSERT INTO user_roles(role, user_id)
VALUES ('USER', 100000),
       ('USER', 100001),
       ('ARCHIVIST', 100001),
       ('ADMIN', 100001),
       ('USER', 100002),
       ('ARCHIVIST', 100002),
       ('USER', 100003);
