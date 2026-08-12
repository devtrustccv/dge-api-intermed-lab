CREATE TABLE IF NOT EXISTS emprego_t_intermediacao (
    id SERIAL PRIMARY KEY,
    tipo_servico VARCHAR(150),
    titulo VARCHAR(250),
    descricao TEXT,
    data_pretendida DATE,
    valor_previsto NUMERIC,
    competencias_exigidas TEXT,
    inicio_candidatura DATE,
    fim_candidatura DATE,
    ilha VARCHAR(100),
    concelho VARCHAR(100),
    zona VARCHAR(150),
    telefone VARCHAR(50),
    email VARCHAR(150),
    anexos JSONB,
    estado VARCHAR(25),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25),
    date_update TIMESTAMP,
    user_update VARCHAR(25)
);

CREATE TABLE IF NOT EXISTS emprego_t_intermediacao_candidato (
    id SERIAL PRIMARY KEY,
    id_intermediacao INTEGER,
    pessoa_id INTEGER,
    nome VARCHAR(150),
    data_candidatura DATE,
    status_candidatura VARCHAR(25),
    selecao_iefp VARCHAR(10),
    status_aceitacao_candidato VARCHAR(25),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25),
    date_update TIMESTAMP,
    user_update VARCHAR(25)
);

CREATE INDEX IF NOT EXISTS idx_emprego_t_intermediacao_candidato_intermediacao
    ON emprego_t_intermediacao_candidato (id_intermediacao);

CREATE INDEX IF NOT EXISTS idx_emprego_t_intermediacao_candidato_pessoa
    ON emprego_t_intermediacao_candidato (pessoa_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_emprego_t_intermediacao_candidato_intermediacao'
    ) THEN
        ALTER TABLE emprego_t_intermediacao_candidato
            ADD CONSTRAINT fk_emprego_t_intermediacao_candidato_intermediacao
            FOREIGN KEY (id_intermediacao)
            REFERENCES emprego_t_intermediacao(id) NOT VALID;
    END IF;
END $$;
