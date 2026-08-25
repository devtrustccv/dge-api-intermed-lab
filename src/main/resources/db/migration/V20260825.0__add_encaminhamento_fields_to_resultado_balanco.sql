ALTER TABLE emprego_t_resultado_balanco
    ADD COLUMN IF NOT EXISTS encaminhamento VARCHAR(25);

ALTER TABLE emprego_t_resultado_balanco
    ADD COLUMN IF NOT EXISTS servico_acolhimento VARCHAR(150);

ALTER TABLE emprego_t_resultado_balanco
    ADD COLUMN IF NOT EXISTS servico_encaminhamento VARCHAR(150);

