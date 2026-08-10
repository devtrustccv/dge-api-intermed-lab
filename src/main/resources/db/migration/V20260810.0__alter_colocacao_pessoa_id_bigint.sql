ALTER TABLE emprego_t_colocacao_candidato
    ALTER COLUMN pessoa_id TYPE BIGINT USING pessoa_id::BIGINT;
