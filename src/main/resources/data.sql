DELETE FROM password_reset_tokens;
DELETE FROM user_roles;
DELETE FROM users;
ALTER SEQUENCE global_seq RESTART WITH 100000;

INSERT INTO users (email, first_name, last_name, password, enabled)
VALUES ('user@gmail.com','John', 'Doe', '{noop}password', true),
       ('admin@gmail.com','Jack', 'London', '{noop}admin', true),
       ('archivist@gmail.com','Viktor', 'Wran', '{noop}password', true),
       ('userDisabled@gmail.com','John', 'Smith', '{noop}password', false);

INSERT INTO user_roles (role, user_id)
VALUES ('USER', 100000),
       ('USER', 100001),
       ('ARCHIVIST', 100001),
       ('ADMIN', 100001),
       ('USER', 100002),
       ('ARCHIVIST', 100002),
       ('USER', 100003);

INSERT INTO password_reset_tokens (user_id, token, expiry_date)
VALUES (100000, 'b5fc98f1-40ec-485e-b316-8453f560bd78', '2052-02-05 12:10:00'),
       (100001, '5a99dd09-d23f-44bb-8d41-b6ff44275d01', '2052-02-05 12:10:00'),
       (100002, '52bde839-9779-4005-b81c-9131c9590d79', '2022-02-06 19:35:56');
