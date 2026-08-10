ALTER TABLE emprego_t_candidatura_oferta
    ALTER COLUMN pessoa_id TYPE BIGINT USING pessoa_id::BIGINT;

ALTER TABLE emprego_t_entrevista_oferta
    ALTER COLUMN pessoa_id TYPE BIGINT USING pessoa_id::BIGINT;

ALTER TABLE emprego_t_candidatura_oferta
    ADD COLUMN IF NOT EXISTS habilitacao_academica VARCHAR(150);

ALTER TABLE emprego_t_candidatura_oferta
    ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;

ALTER TABLE emprego_t_candidatura_oferta
    ADD COLUMN IF NOT EXISTS user_update VARCHAR(25);

ALTER TABLE emprego_t_entrevista_oferta
    ADD COLUMN IF NOT EXISTS estado VARCHAR(25);
