INSERT INTO product_categories (name, art_name)
VALUES ('COMPUTER & ZUBEHÖR','COM');
INSERT INTO product_categories (name, art_name)
VALUES ('BELEUCHTUNG & LED','BL');
INSERT INTO product_categories (name, art_name)
VALUES ('ENERGIE & AKKUS','AKK');
INSERT INTO product_categories (name, art_name)
VALUES ('SMART HOME','SH');
INSERT INTO product_categories (name, art_name)
VALUES ('SMARTPHONES & TELEFONE','TEL');
INSERT INTO product_categories (name, art_name)
VALUES ('NETZWERK & INTERNET','NETZ');
INSERT INTO product_categories (name, art_name)
VALUES ('ELEKTRIK','EL');

INSERT INTO account (email, password, role, state)
VALUES ('admin@gmail.com',
        '$2a$10$ogZC6BcipB1g3sdxRJpv0eCPDTl53UMSWIXvXR/Yn0qy.I2qt5q5S',
        'ADMIN',
        'CONFIRMED');

SHOW INDEX FROM customers WHERE Column_name = 'customer_number';
## DROP INDEX index_name ON customers;
-- где index_name надо заменить на имя индекса, которое находится в колонке Key_name в таблице с индексами
