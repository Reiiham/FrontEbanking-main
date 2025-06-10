INSERT INTO global_settings (setting_key, setting_value) VALUES

                                                             ('maintenance_mode', 'false'),
                                                             ('contact_support_email', 'support@bank.com'),

                                                             ('duree_session_admin', '60'),
                                                             ('duree_session_agent', '45'),
                                                             ('duree_session_client', '15'),

                                                             ('plafond_virement', '10000'),
                                                             ('frais_virement_pourcentage', '2.5'),
                                                             ('plafond_recharge', '200'),
                                                             ('delai_validation_virement', '48'),
                                                             ('plafond_retrait', '5000'),
                                                             ('plafond_achat_en_ligne', '3000'),

                                                             ('expiry_date', '2025-12-31'), -- exemple de date ISO
                                                             ('nb_max_tentatives_connexion', '5'),
                                                             ('delai_blocage', '30'),

                                                             ('niveau_journalisation', 'INFO'),
                                                             ('activer_audit_transactions', 'true');

UPDATE global_settings SET description = 'Active ou desactive le mode maintenance du systeme'
WHERE setting_key = 'maintenance_mode';

UPDATE global_settings SET description = 'Adresse email du support client'
WHERE setting_key = 'contact_support_email';

UPDATE global_settings SET description = 'Duree de session (en minutes) pour les administrateurs'
WHERE setting_key = 'duree_session_admin';

UPDATE global_settings SET description = 'Duree de session (en minutes) pour les agents'
WHERE setting_key = 'duree_session_agent';

UPDATE global_settings SET description = 'Duree de session (en minutes) pour les clients'
WHERE setting_key = 'duree_session_client';

UPDATE global_settings SET description = 'Montant maximum autorise pour un virement (en monnaie locale)'
WHERE setting_key = 'plafond_virement';

UPDATE global_settings SET description = 'Frais appliques aux virements (en pourcentage)'
WHERE setting_key = 'frais_virement_pourcentage';

UPDATE global_settings SET description = 'Montant maximum autorise pour une recharge'
WHERE setting_key = 'plafond_recharge';

UPDATE global_settings SET description = 'Delai de validation d''un virement (en heures)'
WHERE setting_key = 'delai_validation_virement';

UPDATE global_settings SET description = 'Montant maximum autorise pour un retrait'
WHERE setting_key = 'plafond_retrait';

UPDATE global_settings SET description = 'Montant maximum autorise pour les achats en ligne'
WHERE setting_key = 'plafond_achat_en_ligne';

UPDATE global_settings SET description = 'Date d''expiration generale des parametres (format ISO)'
WHERE setting_key = 'expiry_date';

UPDATE global_settings SET description = 'Nombre maximum de tentatives de connexion avant blocage'
WHERE setting_key = 'nb_max_tentatives_connexion';

UPDATE global_settings SET description = 'Duree du blocage en cas d''echec de connexion (en minutes)'
WHERE setting_key = 'delai_blocage';

UPDATE global_settings SET description = 'Niveau de journalisation des logs du systeme'
WHERE setting_key = 'niveau_journalisation';

UPDATE global_settings SET description = 'Activer ou desactiver l''audit des transactions'
WHERE setting_key = 'activer_audit_transactions';
