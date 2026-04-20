CREATE TABLE IF NOT EXISTS emprego_t_utente (
    id SERIAL PRIMARY KEY,
    pessoa_id INTEGER,
    nome VARCHAR(500),
    data_nascimento DATE,
    tipo_documento VARCHAR(25),
    num_documento VARCHAR(25),
    sexo VARCHAR(25),
    nif INTEGER,
    habilitacao_literaria VARCHAR(255),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(150),
    date_update TIMESTAMP,
    user_update VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS emprego_t_entidade (
    id SERIAL PRIMARY KEY,
    denominacao VARCHAR(250),
    global_id_entidade INTEGER,
    nif INTEGER,
    registo_social VARCHAR(150),
    natureza_juridica VARCHAR(150),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(150),
    date_update TIMESTAMP,
    user_update VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS emprego_t_detalhes_emprego_utente (
    id SERIAL PRIMARY KEY,
    id_pessoa INTEGER,
    id_utente INTEGER,
    situacao_emprego VARCHAR(50),
    profissao VARCHAR(255),
    empresa VARCHAR(255),
    setor_atividade VARCHAR(255),
    ilha VARCHAR(100),
    concelho VARCHAR(100),
    zona VARCHAR(100),
    telefone VARCHAR(50),
    num_trabalhador VARCHAR(50),
    duracao VARCHAR(100),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(150),
    date_update TIMESTAMP,
    user_update VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS emprego_t_detalhes_acolhimento (
    id SERIAL PRIMARY KEY,
    id_pessoa INTEGER,
    id_utente INTEGER,
    id_entidade INTEGER,
    denominacao_utente VARCHAR(150),
    nif NUMERIC,
    cefp_id INTEGER,
    org_id INTEGER,
    tipo_utente VARCHAR(50),
    tipo_utente_desc VARCHAR(250),
    tipo_servico VARCHAR(150),
    tipo_servico_desc VARCHAR(250),
    canal VARCHAR(20),
    canal_desc VARCHAR(50),
    detalhes JSONB,
    id_tecnico_atendimento INTEGER,
    tecnico_atendimento VARCHAR(150),
    fonte_informacao VARCHAR(25),
    status_entrevista VARCHAR(50),
    num_inscricao VARCHAR(100),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(500),
    date_update TIMESTAMP,
    user_update VARCHAR(500),
    FOREIGN KEY (id_utente) REFERENCES emprego_t_utente(id),
    FOREIGN KEY (id_entidade) REFERENCES emprego_t_entidade(id)
);

CREATE TABLE IF NOT EXISTS emprego_t_agendamento_entrevista (
    id SERIAL PRIMARY KEY,
    id_utente INTEGER,
    data_entrevista DATE,
    hora_inicio TIME,
    hora_fim TIME,
    id_tecnico INTEGER,
    nome_tecnico VARCHAR(150),
    canal VARCHAR(50),
    local_entrevista VARCHAR(255),
    dm_status_entrevista VARCHAR(50),
    id_cefp INTEGER,
    cefp VARCHAR(255),
    id_acolhimento INTEGER,
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(150),
    date_update TIMESTAMP,
    user_update VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS emprego_t_agendamento_entrevista_hist (
    id SERIAL PRIMARY KEY,
    id_agendamento INTEGER,
    data_anterior DATE,
    horario_inicio_anterior TIME,
    horario_fim_anterior TIME,
    tecnico_anterior_id INTEGER,
    tecnico_anterior_nome VARCHAR(150),
    nova_data_entrevista DATE,
    novo_horario_inicio TIME,
    novo_horario_fim TIME,
    novo_tecnico_id INTEGER,
    novo_tecnico_nome VARCHAR(150),
    justificativa VARCHAR(500),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS emprego_t_notificacao (
    id SERIAL PRIMARY KEY,
    data_registo TIMESTAMP,
    assunto VARCHAR(255),
    data_envio TIMESTAMP,
    mensagem TEXT,
    email VARCHAR(255),
    telemovel VARCHAR(50),
    estado VARCHAR(10),
    flag_leitura VARCHAR(5),
    tipo VARCHAR(50),
    de_email VARCHAR(255),
    app_code VARCHAR(50),
    status_notificacao VARCHAR(50),
    tipo_processo VARCHAR(50)
);
