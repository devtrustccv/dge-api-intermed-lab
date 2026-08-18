CREATE TABLE IF NOT EXISTS emprego_t_certificado_estagio (
    id SERIAL PRIMARY KEY,
    colocacao_id INTEGER NOT NULL,
    pessoa_id BIGINT NOT NULL,
    candidatura_id INTEGER,
    nome VARCHAR(150) NOT NULL,
    naturalidade VARCHAR(150),
    data_nascimento DATE,
    num_documento VARCHAR(100),
    habilitacao_academica VARCHAR(150),
    nome_entidade VARCHAR(150),
    data_inicio DATE,
    data_fim DATE,
    classificacao_final NUMERIC(10, 2),
    assinatura TEXT NOT NULL,
    codigo_contraprova VARCHAR(50) NOT NULL,
    data_emissao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(150) NOT NULL,
    CONSTRAINT uq_certificado_estagio_colocacao UNIQUE (colocacao_id),
    CONSTRAINT uq_certificado_estagio_contraprova UNIQUE (codigo_contraprova)
);

CREATE INDEX IF NOT EXISTS idx_certificado_estagio_pessoa
    ON emprego_t_certificado_estagio (pessoa_id);

