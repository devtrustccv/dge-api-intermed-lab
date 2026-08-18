CREATE TABLE IF NOT EXISTS emprego_t_alerta_config (
    id SERIAL PRIMARY KEY,
    pessoa_id BIGINT,
    tipo_oferta VARCHAR(25),
    titulo_oferta VARCHAR(150),
    ilha VARCHAR(10),
    concelho VARCHAR(10),
    entidade_id INTEGER,
    habilitacao_literaria VARCHAR(25),
    nivel_qualificacao VARCHAR(25),
    estado VARCHAR(25),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(50),
    date_update TIMESTAMP,
    user_update VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_emprego_t_alerta_config_pessoa
    ON emprego_t_alerta_config (pessoa_id);
