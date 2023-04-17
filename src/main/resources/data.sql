DELETE FROM contact_persons;
DELETE FROM companies;
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
VALUES (100001, '5a99dd09-d23f-44bb-8d41-b6ff44275d01', '2052-02-05 12:10:00'),
       (100002, '52bde839-9779-4005-b81c-9131c9590d79', '2022-02-06 19:35:56');

INSERT INTO companies (name, country, city, street, house, zipcode, phone, fax, email)
VALUES ('PAO "TTK"', 'Russia', 'Moscow', 'Aviamotornaya', '32', '121748', '8(495)745-13-22', '8(495)745-13-21', 'ttk@mail.com'),
       ('AO "Super Systems"', 'Russia', 'St Petersburg', 'Nevsky avenue', '42 b2', '134896', '8(498)332-11-45', null, 'supsystems@yandex.ru'),
       ('OOO "Custom Solutions"', 'Russia', 'Tver', 'Kominterna', '20', '114785', '8(564)662-28-15', null, null);

INSERT INTO contact_persons (company_id, position, first_name, middle_name, last_name, phone)
VALUES (100006, 'Director', 'Pavel', 'Ivanovich', 'Ivanov', '8(495) 741-25-17'),
       (100007, 'Chief engineer', 'Ivan', 'Alexandrovich', 'Petrov', '8(745) 111-25-89'),
       (100007, 'Secretary', 'Anna', 'Ivanovna', 'Belkina', '8(745) 111-25-89');
