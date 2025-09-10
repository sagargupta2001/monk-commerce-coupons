-- Products
INSERT INTO product (id, name, price) VALUES (1, 'X', 100);
INSERT INTO product (id, name, price) VALUES (2, 'Y', 200);
INSERT INTO product (id, name, price) VALUES (3, 'Z', 300);
INSERT INTO product (id, name, price) VALUES (4, 'A', 50);
INSERT INTO product (id, name, price) VALUES (5, 'B', 75);
INSERT INTO product (id, name, price) VALUES (6, 'C', 120);

-- Coupon: Buy 2 from [X, Y, Z], Get 1 from [A, B, C]
INSERT INTO coupon (id, type, name, repetition_limit, details_json)
VALUES (1, 'BXGY', 'Buy 2 Get 1', 3,
        '{
            "buyProducts": [1,2,3],
            "getProducts": [4,5,6],
            "buyQuantity": 2,
            "getQuantity": 1,
            "repetitionLimit": 3
        }');
