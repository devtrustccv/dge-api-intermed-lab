ALTER TABLE emprego_t_visitas
    ADD COLUMN IF NOT EXISTS detalhes_avaliacao JSONB;

ALTER TABLE emprego_t_cefp
    ADD COLUMN IF NOT EXISTS nif VARCHAR(50);
