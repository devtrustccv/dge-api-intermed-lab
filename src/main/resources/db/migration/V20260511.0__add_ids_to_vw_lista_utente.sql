CREATE OR REPLACE VIEW vw_lista_utente AS
SELECT
    ('U_' || u.id) AS id,
    'CIDADAO UTENTE' AS tipo_utente,
    u.nome AS nome_utente,
    CAST(u.nif AS VARCHAR) AS nif,
    u.date_create AS data_registo,
    u.pessoa_id AS id_pessoa,
    CAST(NULL AS INTEGER) AS id_entidade
FROM emprego_t_utente u

UNION ALL

SELECT
    ('E_' || e.id) AS id,
    'ENTIDADE EMPREGADORA' AS tipo_utente,
    e.denominacao AS nome_utente,
    CAST(e.nif AS VARCHAR) AS nif,
    e.date_create AS data_registo,
    CAST(NULL AS INTEGER) AS id_pessoa,
    e.global_id_entidade AS id_entidade
FROM emprego_t_entidade e;
