INSERT INTO users (first_name, last_name, email, tel, birth_date, role, password)
VALUES ('Siham', 'siham', 'admin@ebanking.com', '0123456789', '2004-05-22', 'ADMIN', '$2a$10$0M/EfEspItRf3IBMzysxXeW8DJgHXCdpDiFf4FrSPrnEMj3i30lme');

-- pour tester siham , mdp : siham

--new
INSERT INTO users (first_name, last_name, email, tel, birth_date, role, password,cin, username, must_change_password)
VALUES ('Siham', 'siham', 'admin@ebanking.com', '0123456789', '2004-05-22', 'ADMIN', '$2a$10$0M/EfEspItRf3IBMzysxXeW8DJgHXCdpDiFf4FrSPrnEMj3i30lme', 'EE9876', 'SB22004', false),
