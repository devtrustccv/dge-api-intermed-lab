ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS candidatos JSONB;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS nova_data DATE;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS detalhes_avaliacao JSONB;

CREATE INDEX IF NOT EXISTS idx_emprego_t_visitas_entidade
    ON emprego_t_visitas (entidade_id);

CREATE INDEX IF NOT EXISTS idx_emprego_t_visitas_estado
    ON emprego_t_visitas (estado);

CREATE INDEX IF NOT EXISTS idx_emprego_t_visitas_cefp
    ON emprego_t_visitas (cefp_id);

CREATE INDEX IF NOT EXISTS idx_emprego_t_visitas_data_visita
    ON emprego_t_visitas (data_visita);
