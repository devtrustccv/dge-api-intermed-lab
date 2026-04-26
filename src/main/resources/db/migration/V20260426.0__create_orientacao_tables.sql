
CREATE TABLE IF NOT EXISTS emprego_t_agendamento_entrevista (
    id SERIAL PRIMARY KEY,
    id_acolhimento INTEGER NOT NULL,
    id_utente INTEGER NOT NULL,
    id_tecnico INTEGER NOT NULL,
    nome_tecnico VARCHAR(555) NOT NULL,
    data_entrevista DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    local VARCHAR(555),
    dm_status_entrevista VARCHAR(25) ,
    date_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_cefp INTEGER,
    cefp VARCHAR(255),
    tipo_servico VARCHAR(50),
    canal VARCHAR(25),
    local_entrevista VARCHAR(555),
    resultado_entrevista JSONB,
    parecer_io VARCHAR(150),
    obs_parecer_io VARCHAR(500),
    path_resultado VARCHAR(1000),
    user_create VARCHAR(255),
    date_update TIMESTAMP,
    user_update VARCHAR(255)
);

ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS id_acolhimento INTEGER;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS id_utente INTEGER;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS id_tecnico INTEGER;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS nome_tecnico VARCHAR(555);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS data_entrevista DATE;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS hora_inicio TIME;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS hora_fim TIME;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS local VARCHAR(555);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS dm_status_entrevista VARCHAR(25);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS id_cefp INTEGER;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS cefp VARCHAR(255);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS tipo_servico VARCHAR(50);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS canal VARCHAR(25);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS local_entrevista VARCHAR(555);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS resultado_entrevista JSONB;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS parecer_io VARCHAR(150);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS obs_parecer_io VARCHAR(500);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS path_resultado VARCHAR(1000);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS user_create VARCHAR(255);
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_agendamento_entrevista ADD COLUMN IF NOT EXISTS user_update VARCHAR(255);

CREATE TABLE IF NOT EXISTS emprego_t_agendamento_balanco (
    id SERIAL PRIMARY KEY,
    id_utente INTEGER NOT NULL,
    id_tecnico INTEGER NOT NULL,
    nome_tecnico VARCHAR(555) NOT NULL,
    data DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    local VARCHAR(555),
    dm_status VARCHAR(25) NOT NULL ,
    date_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_cefp INTEGER,
    cefp VARCHAR(255),
    canal VARCHAR(25),
    tipo_balanco VARCHAR(25),
    entrevista_id INTEGER,
    resultado_balanco VARCHAR(2500),
    date_resultado_balanco TIMESTAMP,
    user_create VARCHAR(255),
    date_update TIMESTAMP,
    user_update VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS emprego_t_resultado_balanco (
    id SERIAL PRIMARY KEY,
    id_agendamento_balanco INTEGER NOT NULL,
    id_acolhimento INTEGER,
    entrevista_id INTEGER,
    id_utente INTEGER,
    id_tecnico INTEGER,
    descricao_resultado VARCHAR(2500),
    date_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(255),
    date_update TIMESTAMP,
    user_update VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS emprego_t_parametrizacao_report (
    id SERIAL PRIMARY KEY,
    tipo_report VARCHAR(50) NOT NULL,
    detalhes JSONB,
    logotipo_iefp VARCHAR(555),
    logotipo_dge VARCHAR(555),
    date_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(55),
    date_update TIMESTAMP,
    user_update VARCHAR(55)
);

ALTER TABLE emprego_t_parametrizacao_report ADD COLUMN IF NOT EXISTS tipo_report VARCHAR(50);
ALTER TABLE emprego_t_parametrizacao_report ADD COLUMN IF NOT EXISTS detalhes JSONB;
ALTER TABLE emprego_t_parametrizacao_report ADD COLUMN IF NOT EXISTS logotipo_iefp VARCHAR(555);
ALTER TABLE emprego_t_parametrizacao_report ADD COLUMN IF NOT EXISTS logotipo_dge VARCHAR(555);
ALTER TABLE emprego_t_parametrizacao_report ADD COLUMN IF NOT EXISTS date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE emprego_t_parametrizacao_report ADD COLUMN IF NOT EXISTS user_create VARCHAR(55);
ALTER TABLE emprego_t_parametrizacao_report ADD COLUMN IF NOT EXISTS date_update TIMESTAMP;
ALTER TABLE emprego_t_parametrizacao_report ADD COLUMN IF NOT EXISTS user_update VARCHAR(55);

CREATE TABLE IF NOT EXISTS emprego_t_acolhimento_servico (
    id SERIAL PRIMARY KEY,
    id_acolhimento INTEGER NOT NULL,
    id_utente INTEGER NOT NULL,
    tipo_utente VARCHAR(50),
    tipo_utente_desc VARCHAR(250),
    tipo_servico VARCHAR(150),
    tipo_servico_desc VARCHAR(250),
    detalhes_servico JSONB,
    id_entrevista INTEGER,
    detalhes_analise JSONB,
    necessidade_analise BOOLEAN NOT NULL DEFAULT FALSE,
    date_create TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(250),
    date_update TIMESTAMP,
    user_update VARCHAR(250)
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_entrevista_acolhimento') THEN
        ALTER TABLE emprego_t_agendamento_entrevista
            ADD CONSTRAINT fk_emprego_t_entrevista_acolhimento
            FOREIGN KEY (id_acolhimento) REFERENCES emprego_t_detalhes_acolhimento(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_entrevista_utente') THEN
        ALTER TABLE emprego_t_agendamento_entrevista
            ADD CONSTRAINT fk_emprego_t_entrevista_utente
            FOREIGN KEY (id_utente) REFERENCES emprego_t_utente(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_entrevista_tecnico') THEN
        ALTER TABLE emprego_t_agendamento_entrevista
            ADD CONSTRAINT fk_emprego_t_entrevista_tecnico
            FOREIGN KEY (id_tecnico) REFERENCES emprego_t_tecnicos(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_entrevista_cefp') THEN
        ALTER TABLE emprego_t_agendamento_entrevista
            ADD CONSTRAINT fk_emprego_t_entrevista_cefp
            FOREIGN KEY (id_cefp) REFERENCES emprego_t_cefp(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_balanco_utente') THEN
        ALTER TABLE emprego_t_agendamento_balanco
            ADD CONSTRAINT fk_emprego_t_balanco_utente
            FOREIGN KEY (id_utente) REFERENCES emprego_t_utente(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_balanco_tecnico') THEN
        ALTER TABLE emprego_t_agendamento_balanco
            ADD CONSTRAINT fk_emprego_t_balanco_tecnico
            FOREIGN KEY (id_tecnico) REFERENCES emprego_t_tecnicos(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_balanco_cefp') THEN
        ALTER TABLE emprego_t_agendamento_balanco
            ADD CONSTRAINT fk_emprego_t_balanco_cefp
            FOREIGN KEY (id_cefp) REFERENCES emprego_t_cefp(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_balanco_entrevista') THEN
        ALTER TABLE emprego_t_agendamento_balanco
            ADD CONSTRAINT fk_emprego_t_balanco_entrevista
            FOREIGN KEY (entrevista_id) REFERENCES emprego_t_agendamento_entrevista(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_resultado_balanco_agendamento') THEN
        ALTER TABLE emprego_t_resultado_balanco
            ADD CONSTRAINT fk_emprego_t_resultado_balanco_agendamento
            FOREIGN KEY (id_agendamento_balanco) REFERENCES emprego_t_agendamento_balanco(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_resultado_balanco_entrevista') THEN
        ALTER TABLE emprego_t_resultado_balanco
            ADD CONSTRAINT fk_emprego_t_resultado_balanco_entrevista
            FOREIGN KEY (entrevista_id) REFERENCES emprego_t_agendamento_entrevista(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_resultado_balanco_utente') THEN
        ALTER TABLE emprego_t_resultado_balanco
            ADD CONSTRAINT fk_emprego_t_resultado_balanco_utente
            FOREIGN KEY (id_utente) REFERENCES emprego_t_utente(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_resultado_balanco_tecnico') THEN
        ALTER TABLE emprego_t_resultado_balanco
            ADD CONSTRAINT fk_emprego_t_resultado_balanco_tecnico
            FOREIGN KEY (id_tecnico) REFERENCES emprego_t_tecnicos(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_servico_acolhimento') THEN
        ALTER TABLE emprego_t_acolhimento_servico
            ADD CONSTRAINT fk_emprego_t_servico_acolhimento
            FOREIGN KEY (id_acolhimento) REFERENCES emprego_t_detalhes_acolhimento(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_servico_utente') THEN
        ALTER TABLE emprego_t_acolhimento_servico
            ADD CONSTRAINT fk_emprego_t_servico_utente
            FOREIGN KEY (id_utente) REFERENCES emprego_t_utente(id) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_emprego_t_servico_entrevista') THEN
        ALTER TABLE emprego_t_acolhimento_servico
            ADD CONSTRAINT fk_emprego_t_servico_entrevista
            FOREIGN KEY (id_entrevista) REFERENCES emprego_t_agendamento_entrevista(id) NOT VALID;
    END IF;
END $$;
