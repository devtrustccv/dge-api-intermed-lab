ALTER TABLE emprego_t_relatorio_acomp
    ALTER COLUMN pessoa_id TYPE BIGINT USING pessoa_id::BIGINT,
    ALTER COLUMN nome TYPE VARCHAR(150),
    ALTER COLUMN denominacao_entidade TYPE VARCHAR(150),
    ALTER COLUMN relatorio_anexo TYPE VARCHAR(500),
    ALTER COLUMN user_create TYPE VARCHAR(150),
    ALTER COLUMN user_update TYPE VARCHAR(150);

CREATE INDEX IF NOT EXISTS idx_emprego_relatorio_acomp_entidade_estado
    ON emprego_t_relatorio_acomp (entidade_id, estado);

CREATE INDEX IF NOT EXISTS idx_emprego_relatorio_acomp_pessoa
    ON emprego_t_relatorio_acomp (pessoa_id);

CREATE INDEX IF NOT EXISTS idx_emprego_relatorio_acomp_colocacao
    ON emprego_t_relatorio_acomp (id_colocacao);
