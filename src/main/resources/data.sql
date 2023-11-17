DELETE FROM changes;
DELETE FROM change_notices;
DELETE FROM sendings;
DELETE FROM invoices;
DELETE FROM letters;
DELETE FROM subscribers;
DELETE FROM document_content_files;
DELETE FROM document_contents;
DELETE FROM documents;
DELETE FROM departments;
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

INSERT INTO contact_persons (company_id, position, last_name, first_name, middle_name, phone)
VALUES (100006, 'Director', 'Ivanov', 'Pavel', 'Ivanovich', '8(495)741-25-17'),
       (100007, 'Chief engineer', 'Petrov', 'Ivan', 'Alexandrovich', '8(745)111-25-89'),
       (100007, 'Secretary', 'Belkina', 'Anna', 'Ivanovna', '8(745)111-25-89');

INSERT INTO departments (name, last_name, first_name, middle_name, phone, email)
VALUES ('KTK-40', 'Sokolov', 'Alexandr', 'Ivanovich', '1-32-98', 'a.sokolov@npo.lan'),
       ('NIO-6', 'Ivanov', 'Petr', 'Alexandrovich', '1-34-63', 'p.ivanov@npo.lan'),
       ('NIO-8', 'Kozlov', 'Ivan', 'Ivanovich', '1-44-12', 'i.kozlov@npo.lan'),
       ('DEP-25', 'Sidorov', 'Alexandr', 'Petrovich', '1-36-78', 'a.sidorov@npo.lan'),
       ('DEP-33', 'Petrov', 'Vladimir', 'Ivanovich', '1-45-12', 'v.petrov@npo.lan');

INSERT INTO documents (name, decimal_number, inventory_number, accounting_date, status, type, symbol, annulled, secret, auto_generated, comment, developer_id, original_holder_id)
VALUES ('Block M21', 'VUIA.465521.004', '926531', '2023-03-24', 'ORIGINAL', 'DIGITAL', 'O1', false, false,  false, 'some comment', 100009, 100008),
       ('Block M21 electric scheme', 'VUIA.465521.004E3', '926532', '2023-03-24', 'ORIGINAL', 'PAPER', null, false, false, false, null, 100011, 100007),
       ('Panel B45', 'UPIA.421478.001-01', '456213', '2021-05-18', 'ORIGINAL', 'DIGITAL', null, false, true, false, null, 100010, 100007),
       ('Block N56 project data', 'VUIA.465521.004D4', '312458', '2018-01-12', 'DUPLICATE', 'PAPER', 'O1', false, false, false, null, null, 100008),
       ('Device BKLV', 'VUIA.685412.003', '325698', '2017-03-15', 'ORIGINAL', 'DIGITAL', 'O', true, false, false, 'some comment', 100011, 100007),
       (null, 'VUIA.652147.001', null, null, null, null, null, false, false, true, null, null, null);

INSERT INTO applicabilities (document_id, applicability_id, primal)
VALUES (100018, 100014, true),
       (100018, 100016, false);

INSERT INTO document_contents (document_id, change_number, created)
VALUES (100014, 0, '2023-02-05 12:10:00'),
       (100014, 1, '2023-05-18 14:05:00'),
       (100014, 2, '2023-07-24 09:28:00'),
       (100015, 0, '2022-03-27 11:40:00');

INSERT INTO document_content_files (document_content_id, file_name, file_link)
VALUES (100022, 'VUIA.465521.004.docx', 'VUIA.465521.004/0/VUIA.465521.004.docx'),
       (100022, 'VUIA.465521.004.pdf', 'VUIA.465521.004/0/VUIA.465521.004.pdf'),
       (100023, 'VUIA.465521.004.docx', 'VUIA.465521.004/1/VUIA.465521.004.docx'),
       (100024, 'VUIA.465521.004.pdf', 'VUIA.465521.004/2/VUIA.465521.004.pdf'),
       (100025, 'List_1.pdf', 'VUIA.465521.004E3/0/List_1.pdf'),
       (100025, 'List_2.pdf', 'VUIA.465521.004E3/0/List_2.pdf');

INSERT INTO subscribers (document_id, company_id, subscribed, doc_status, unsubscribe_timestamp, unsubscribe_reason)
VALUES (100014, 100006, true, 'DUPLICATE', null, null), --same invoice (and letter)
       (100014, 100007, false, 'UNACCOUNTED_COPY', null, null),
       (100014, 100008, false, 'DUPLICATE', '2022-10-14 11:35:00', 'Letter # 2368-456 dated 2022-09-25'),
       (100015, 100006, true, 'ACCOUNTED_COPY', null, null), --same invoice (and letter)
       (100015, 100008, true, 'ACCOUNTED_COPY', null, null);

INSERT INTO letters (number, date, company_id)
VALUES (null, null, 100006), --has two documents in invoice
       ('15/49-3256', '2019-02-14', 100006), -- second letter for subscriber
       (null, null, 100007),
       ('15/49-1456', '2016-05-20', 100008),
       (null, null, 100008);

INSERT INTO invoices (number, date, doc_status, letter_id)
VALUES ('75', '2018-03-16', 'ACCOUNTED_COPY', 100031), --has two documents
       ('84', '2019-02-12', 'DUPLICATE', 100032),
       ('11', '2017-07-15', 'UNACCOUNTED_COPY', 100033),
       ('21', '2017-09-18', 'DUPLICATE', 100034),
       ('33', '2016-01-12', 'ACCOUNTED_COPY', 100035);

INSERT INTO sendings (document_id, invoice_id)
VALUES (100014, 100036), --same invoice (and letter)
       (100014, 100037),
       (100014, 100038),
       (100014, 100039),
       (100015, 100036), --same invoice (and letter)
       (100015, 100040);

INSERT INTO change_notices (name, release_date, auto_generated)
VALUES ('VUIA.SK.591', '2020-06-18', true),
       ('VUIA.TN.429', '2021-12-14', true);

INSERT INTO changes (document_id, change_notice_id, change_number)
VALUES (100014, 100047, 1),
       (100014, 100048, 2),
       (100015, 100047, 1);
