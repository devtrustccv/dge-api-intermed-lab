CREATE TABLE IF NOT EXISTS emprego_t_adesao (
    id SERIAL PRIMARY KEY,
    pessoa_id BIGINT,
    situacao_profissional VARCHAR(25),
    id_utente INTEGER,
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_emprego_t_adesao_pessoa
    ON emprego_t_adesao (pessoa_id);

CREATE INDEX IF NOT EXISTS idx_emprego_t_adesao_utente
    ON emprego_t_adesao (id_utente);
