ALTER TABLE IF EXISTS emprego_t_avaliacao_estagiario
    ALTER COLUMN sugestao TYPE JSONB
    USING CASE
        WHEN sugestao IS NULL OR BTRIM(sugestao) = '' THEN '[]'::JSONB
        ELSE JSONB_BUILD_ARRAY(sugestao)
    END;
