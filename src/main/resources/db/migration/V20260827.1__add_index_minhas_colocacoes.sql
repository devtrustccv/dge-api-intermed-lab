CREATE INDEX IF NOT EXISTS idx_emprego_colocacao_pessoa_data
    ON emprego_t_colocacao_candidato (pessoa_id, date_create DESC, id DESC);
