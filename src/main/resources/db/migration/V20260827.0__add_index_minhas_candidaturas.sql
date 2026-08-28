CREATE INDEX IF NOT EXISTS idx_emprego_candidatura_pessoa_data
    ON emprego_t_candidatura_oferta (pessoa_id, date_create DESC, id DESC);
