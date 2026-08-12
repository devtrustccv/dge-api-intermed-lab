ALTER TABLE emprego_t_assiduidade
    ALTER COLUMN pessoa_id TYPE BIGINT
    USING pessoa_id::BIGINT;
