ALTER TABLE emprego_t_intermediacao
    ADD COLUMN IF NOT EXISTS contratante_id BIGINT;

ALTER TABLE emprego_t_intermediacao
    ADD COLUMN IF NOT EXISTS nome VARCHAR(150);

ALTER TABLE emprego_t_intermediacao_candidato
    ALTER COLUMN pessoa_id TYPE BIGINT
    USING pessoa_id::BIGINT;

CREATE INDEX IF NOT EXISTS idx_emprego_t_intermediacao_contratante_data
    ON emprego_t_intermediacao (contratante_id, date_create DESC, id DESC);
