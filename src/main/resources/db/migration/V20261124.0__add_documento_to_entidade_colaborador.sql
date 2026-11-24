ALTER TABLE emprego_t_entidade_colaborador
    ADD COLUMN IF NOT EXISTS tipo_documento TEXT;

ALTER TABLE emprego_t_entidade_colaborador
    ADD COLUMN IF NOT EXISTS num_documento TEXT;
