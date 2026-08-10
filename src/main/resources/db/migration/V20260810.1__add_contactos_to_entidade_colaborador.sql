ALTER TABLE emprego_t_entidade_colaborador
    ADD COLUMN IF NOT EXISTS email VARCHAR(150);

ALTER TABLE emprego_t_entidade_colaborador
    ADD COLUMN IF NOT EXISTS telemovel VARCHAR(25);
