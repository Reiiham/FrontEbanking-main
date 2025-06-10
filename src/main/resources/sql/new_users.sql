INSERT INTO users (
    birth_date, compte_bloque, documents_complets, must_change_password,
    id, token_expiry, tel, cin, email, first_name, last_name,
    password, role, token, username
) VALUES
      ('1995-05-10', false, true, false, 1, NULL, '0612345678', 'AB123456', 'employee@example.com', 'John', 'Doe',
       '$2a$10$0M/EfEspItRf3IBMzysxXeW8DJgHXCdpDiFf4FrSPrnEMj3i30lme', 'EMPLOYEE', NULL, 'johndoe'),

      ('1985-05-12', false, true, NULL, 2, NULL, '061293477', 'YZ39275', 'Lataniawest@examplle.com', 'Latania', 'West',
       '$2a$10$0M/EfEspItRf3IBMzysxXeW8DJgHXCdpDiFf4FrSPrnEMj3i30lme', 'CLIENT', NULL, 'XMC0871'),

      ('1985-05-12', false, true, NULL, 3, NULL, '061293470', 'YZ39207', 'siham.belahcen@hotmail.com', 'kuromi', 'West',
       '$2a$10$0M/EfEspItRf3IBMzysxXeW8DJgHXCdpDiFf4FrSPrnEMj3i30lme', 'CLIENT', NULL, 'QXM6534'),

      ('2004-05-22', false, true, NULL, 5, NULL, '0123456789', 'EE9876', 'admin@ebanking.com', 'Siham', 'siham',
       '$2a$10$0M/EfEspItRf3IBMzysxXeW8DJgHXCdpDiFf4FrSPrnEMj3i30lme', 'ADMIN', NULL, 'SB22004'),

      ('1985-05-12', false, true, NULL, 4, NULL, '061293470', 'YZ39209', 'siham.belahcen@hotmail.com', 'kuromi', 'West',
       '$2a$10$0M/EfEspItRf3IBMzysxXeW8DJgHXCdpDiFf4FrSPrnEMj3i30lme', 'CLIENT', NULL, 'DNH4004');
-- mdp est siham


INSERT INTO accounts (
    balance, user_id, accountnumber, id, rib, type
) VALUES
      (295000, 3, 'I0a9QU1IIC1EkyO49opaCQ==', '28a5c45c-0846-47a6-a145-cf2614a0dec3', '12345678900027230234091', 'courant'),

      (294000, 4, 'bMcD2uEJDyl3WKt7emHydQ==', 'dd287a57-458b-42a9-a0e2-1d5859e1323a', '12345678900063359753612', 'courant'),

      (296000, 2, 'MG1eBBENI1qG1k6Gb5DEVw==', '12deba08-bf46-4719-afdf-b20efd374a1a', '12345678900092812631431', 'courant');


INSERT INTO user_servicesactifs (user_id, servicesactifs) VALUES
                                                              (4, 'VIREMENTS'),
                                                              (4, 'ACCES_EN_LIGNE'),
                                                              (3, 'VIREMENTS'),
                                                              (3, 'ACCES_EN_LIGNE'),
                                                              (2, 'VIREMENTS'),
                                                              (2, 'ACCES_EN_LIGNE');


