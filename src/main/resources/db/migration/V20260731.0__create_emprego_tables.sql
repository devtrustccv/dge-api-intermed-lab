CREATE TABLE IF NOT EXISTS emprego_t_oferta (
    id SERIAL PRIMARY KEY,
    codigo_referencia VARCHAR(25),
    tipo_oferta VARCHAR(25),
    titulo VARCHAR(150),
    descricao VARCHAR(500),
    data_inicio_candidatura DATE,
    data_fim_candidatura DATE,
    data_inicio_previsto DATE,
    duracao_contrato INTEGER,
    regime_contrato VARCHAR(25),
    entidade_id INTEGER,
    denominacao_entidade VARCHAR(50),
    habilitacao_minima VARCHAR(25),
    nivel_qualificacao VARCHAR(25),
    num_vagas INTEGER,
    habilitacao_maxima VARCHAR(25),
    conhecimento_linguistico JSONB,
    competencias_valorizadas JSONB,
    hora_inicio TIME,
    hora_fim TIME,
    dias_semana JSONB,
    cursos_area_formacao JSONB,
    experiencia_profissional JSONB,
    ilha VARCHAR(100),
    concelho VARCHAR(100),
    orientador_id INTEGER,
    email_contacto VARCHAR(50),
    contacto VARCHAR(15),
    observacao VARCHAR(500),
    estado VARCHAR(25),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(50),
    date_update TIMESTAMP,
    user_update VARCHAR(50)
);

ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS id INTEGER;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS codigo_referencia VARCHAR(25);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS tipo_oferta VARCHAR(25);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS titulo VARCHAR(150);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS descricao VARCHAR(500);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS data_inicio_candidatura DATE;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS data_fim_candidatura DATE;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS data_inicio_previsto DATE;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS duracao_contrato INTEGER;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS regime_contrato VARCHAR(25);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS entidade_id INTEGER;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS denominacao_entidade VARCHAR(50);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS habilitacao_minima VARCHAR(25);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS nivel_qualificacao VARCHAR(25);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS num_vagas INTEGER;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS habilitacao_maxima VARCHAR(25);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS conhecimento_linguistico JSONB;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS competencias_valorizadas JSONB;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS hora_inicio TIME;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS hora_fim TIME;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS dias_semana JSONB;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS cursos_area_formacao JSONB;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS experiencia_profissional JSONB;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS ilha VARCHAR(100);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS concelho VARCHAR(100);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS orientador_id INTEGER;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS email_contacto VARCHAR(50);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS contacto VARCHAR(15);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS observacao VARCHAR(500);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS estado VARCHAR(25);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS user_create VARCHAR(50);
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_oferta ADD COLUMN IF NOT EXISTS user_update VARCHAR(50);

CREATE TABLE IF NOT EXISTS emprego_t_entidade_colaborador (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150),
    pessoa_id INTEGER,
    cargo VARCHAR(25),
    estado VARCHAR(25),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25),
    date_update TIMESTAMP,
    user_update VARCHAR(25)
);

ALTER TABLE emprego_t_entidade_colaborador ADD COLUMN IF NOT EXISTS id INTEGER;
ALTER TABLE emprego_t_entidade_colaborador ADD COLUMN IF NOT EXISTS nome VARCHAR(150);
ALTER TABLE emprego_t_entidade_colaborador ADD COLUMN IF NOT EXISTS pessoa_id INTEGER;
ALTER TABLE emprego_t_entidade_colaborador ADD COLUMN IF NOT EXISTS cargo VARCHAR(25);
ALTER TABLE emprego_t_entidade_colaborador ADD COLUMN IF NOT EXISTS estado VARCHAR(25);
ALTER TABLE emprego_t_entidade_colaborador ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_entidade_colaborador ADD COLUMN IF NOT EXISTS user_create VARCHAR(25);
ALTER TABLE emprego_t_entidade_colaborador ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_entidade_colaborador ADD COLUMN IF NOT EXISTS user_update VARCHAR(25);

CREATE TABLE IF NOT EXISTS emprego_t_candidatura_oferta (
    id SERIAL PRIMARY KEY,
    pessoa_id INTEGER,
    nome VARCHAR(150),
    tipo_oferta VARCHAR(25),
    id_oferta INTEGER,
    entidade_id INTEGER,
    selecao_iefp BOOLEAN,
    canal VARCHAR(25),
    status_candidatura VARCHAR(25),
    anexos JSONB,
    motivo_recusa VARCHAR(250),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25)
);

ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS id INTEGER;
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS pessoa_id INTEGER;
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS nome VARCHAR(150);
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS tipo_oferta VARCHAR(25);
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS id_oferta INTEGER;
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS entidade_id INTEGER;
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS selecao_iefp BOOLEAN;
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS canal VARCHAR(25);
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS status_candidatura VARCHAR(25);
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS anexos JSONB;
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS motivo_recusa VARCHAR(250);
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_candidatura_oferta ADD COLUMN IF NOT EXISTS user_create VARCHAR(25);

CREATE TABLE IF NOT EXISTS emprego_t_entrevista_oferta (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150),
    pessoa_id INTEGER,
    id_candidatura INTEGER,
    data_entrevista DATE,
    horario TIME,
    canal VARCHAR(25),
    local_entrevista VARCHAR(150),
    parecer_entrevista VARCHAR(25),
    resultado_entrevista VARCHAR(150),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25),
    date_update TIMESTAMP,
    user_update VARCHAR(25)
);

ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS id INTEGER;
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS nome VARCHAR(150);
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS pessoa_id INTEGER;
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS id_candidatura INTEGER;
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS data_entrevista DATE;
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS horario TIME;
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS canal VARCHAR(25);
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS local_entrevista VARCHAR(150);
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS parecer_entrevista VARCHAR(25);
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS resultado_entrevista VARCHAR(150);
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS user_create VARCHAR(25);
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_entrevista_oferta ADD COLUMN IF NOT EXISTS user_update VARCHAR(25);

CREATE TABLE IF NOT EXISTS emprego_t_colocacao_candidato (
    id SERIAL PRIMARY KEY,
    id_oferta INTEGER,
    tipo_oferta VARCHAR(25),
    codigo_referencia VARCHAR(25),
    entidade_id INTEGER,
    denominacao_entidade VARCHAR(150),
    pessoa_id INTEGER,
    nome VARCHAR(150),
    id_candidatura INTEGER,
    data_inicio_previsto DATE,
    data_fim_previsto DATE,
    tipo_contrato VARCHAR(25),
    duracao_contrato INTEGER,
    descricao VARCHAR(250),
    contrato_path VARCHAR(150),
    estado VARCHAR(25),
    registado_cefp BOOLEAN,
    id_organica INTEGER,
    cefp VARCHAR(150),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25),
    date_update TIMESTAMP,
    user_update VARCHAR(25)
);

ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS id INTEGER;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS id_oferta INTEGER;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS tipo_oferta VARCHAR(25);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS codigo_referencia VARCHAR(25);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS entidade_id INTEGER;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS denominacao_entidade VARCHAR(150);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS pessoa_id INTEGER;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS nome VARCHAR(150);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS id_candidatura INTEGER;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS data_inicio_previsto DATE;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS data_fim_previsto DATE;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS tipo_contrato VARCHAR(25);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS duracao_contrato INTEGER;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS descricao VARCHAR(250);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS contrato_path VARCHAR(150);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS estado VARCHAR(25);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS registado_cefp BOOLEAN;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS id_organica INTEGER;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS cefp VARCHAR(150);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS user_create VARCHAR(25);
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_colocacao_candidato ADD COLUMN IF NOT EXISTS user_update VARCHAR(25);

CREATE TABLE IF NOT EXISTS emprego_t_avaliacao_estagiario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150),
    pessoa_id INTEGER,
    candidatura_id INTEGER,
    tipo_avaliacao VARCHAR(25),
    periodo_referencia VARCHAR(25),
    avaliacao_desempenho JSONB,
    grau_satisfacao VARCHAR(25),
    interesse_contratacao VARCHAR(150),
    classificacao VARCHAR(5),
    observacao VARCHAR(250),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25),
    date_update TIMESTAMP,
    user_update VARCHAR(25)
);

ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS id INTEGER;
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS nome VARCHAR(150);
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS pessoa_id INTEGER;
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS candidatura_id INTEGER;
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS tipo_avaliacao VARCHAR(25);
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS periodo_referencia VARCHAR(25);
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS avaliacao_desempenho JSONB;
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS grau_satisfacao VARCHAR(25);
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS interesse_contratacao VARCHAR(150);
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS classificacao VARCHAR(5);
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS observacao VARCHAR(250);
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS user_create VARCHAR(25);
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_avaliacao_estagiario ADD COLUMN IF NOT EXISTS user_update VARCHAR(25);

CREATE TABLE IF NOT EXISTS emprego_t_visitas (
    id SERIAL PRIMARY KEY,
    entidade_id INTEGER,
    visitante VARCHAR(150),
    data_visita DATE,
    hora_inicio TIME,
    hora_fim TIME,
    objetivos VARCHAR(250),
    estado VARCHAR(25),
    conteudo_reuniao VARCHAR(500),
    observacoes_entidade VARCHAR(500),
    observacoes_iefp VARCHAR(500),
    supervisor_participante VARCHAR(150),
    agendado_por VARCHAR(150),
    motivo_indeferimento VARCHAR(250),
    cefp_id INTEGER,
    cefp VARCHAR(150),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25),
    date_update TIMESTAMP,
    user_update VARCHAR(25)
);

ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS id INTEGER;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS entidade_id INTEGER;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS visitante VARCHAR(150);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS data_visita DATE;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS hora_inicio TIME;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS hora_fim TIME;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS objetivos VARCHAR(250);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS estado VARCHAR(25);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS conteudo_reuniao VARCHAR(500);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS observacoes_entidade VARCHAR(500);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS observacoes_iefp VARCHAR(500);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS supervisor_participante VARCHAR(150);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS agendado_por VARCHAR(150);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS motivo_indeferimento VARCHAR(250);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS cefp_id INTEGER;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS cefp VARCHAR(150);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS user_create VARCHAR(25);
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_visitas ADD COLUMN IF NOT EXISTS user_update VARCHAR(25);

CREATE TABLE IF NOT EXISTS emprego_t_assiduidade (
    id SERIAL PRIMARY KEY,
    id_colocacao INTEGER,
    entidade_id INTEGER,
    denominacao_entidade VARCHAR(50),
    pessoa_id INTEGER,
    nome VARCHAR(50),
    data DATE,
    hora_entrada TIME,
    hora_saida TIME,
    tipo_assiduidade VARCHAR(25),
    justificacao VARCHAR(500),
    estado VARCHAR(25),
    observacao VARCHAR(500),
    comprovativo VARCHAR(150),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25),
    date_update TIMESTAMP,
    user_update VARCHAR(25)
);

ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS id INTEGER;
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS id_colocacao INTEGER;
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS entidade_id INTEGER;
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS denominacao_entidade VARCHAR(50);
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS pessoa_id INTEGER;
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS nome VARCHAR(50);
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS data DATE;
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS hora_entrada TIME;
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS hora_saida TIME;
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS tipo_assiduidade VARCHAR(25);
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS justificacao VARCHAR(500);
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS estado VARCHAR(25);
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS observacao VARCHAR(500);
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS comprovativo VARCHAR(150);
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS user_create VARCHAR(25);
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_assiduidade ADD COLUMN IF NOT EXISTS user_update VARCHAR(25);

CREATE TABLE IF NOT EXISTS emprego_t_relatorio_acomp (
    id SERIAL PRIMARY KEY,
    id_oferta INTEGER,
    id_colocacao INTEGER,
    entidade_id INTEGER,
    denominacao_entidade VARCHAR(50),
    pessoa_id INTEGER,
    nome VARCHAR(50),
    data_inicio DATE,
    data_fim DATE,
    atividades_realizadas VARCHAR(500),
    dificuldades VARCHAR(500),
    recomendacoes VARCHAR(500),
    estado VARCHAR(25),
    relatorio_anexo VARCHAR(150),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(25),
    date_update TIMESTAMP,
    user_update VARCHAR(25)
);

ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS id INTEGER;
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS id_oferta INTEGER;
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS id_colocacao INTEGER;
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS entidade_id INTEGER;
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS denominacao_entidade VARCHAR(50);
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS pessoa_id INTEGER;
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS nome VARCHAR(50);
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS data_inicio DATE;
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS data_fim DATE;
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS atividades_realizadas VARCHAR(500);
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS dificuldades VARCHAR(500);
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS recomendacoes VARCHAR(500);
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS estado VARCHAR(25);
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS relatorio_anexo VARCHAR(150);
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS user_create VARCHAR(25);
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_relatorio_acomp ADD COLUMN IF NOT EXISTS user_update VARCHAR(25);
