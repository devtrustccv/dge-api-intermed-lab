CREATE OR REPLACE VIEW vw_lista_utente AS
SELECT
    ('U_' || u.id) AS id,
    'CIDADAO UTENTE' AS tipo_utente,
    u.nome AS nome_utente,
    CAST(u.nif AS VARCHAR) AS nif,
    u.date_create AS data_registo
FROM emprego_t_utente u

UNION ALL

SELECT
    ('E_' || e.id) AS id,
    'ENTIDADE EMPREGADORA' AS tipo_utente,
    e.denominacao AS nome_utente,
    CAST(e.nif AS VARCHAR) AS nif,
    e.date_create AS data_registo
FROM emprego_t_entidade e;
