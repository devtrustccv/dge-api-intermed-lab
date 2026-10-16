ALTER TABLE IF EXISTS emprego_t_avaliacao_estagiario
    ADD COLUMN IF NOT EXISTS sugestao TEXT;

ALTER TABLE IF EXISTS emprego_t_avaliacao_estagiario
    ADD COLUMN IF NOT EXISTS desenvolvimento_atividade TEXT;
