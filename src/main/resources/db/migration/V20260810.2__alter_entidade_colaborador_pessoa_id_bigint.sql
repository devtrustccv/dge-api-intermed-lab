ALTER TABLE emprego_t_entidade_colaborador
    ALTER COLUMN pessoa_id TYPE BIGINT
    USING pessoa_id::BIGINT;
