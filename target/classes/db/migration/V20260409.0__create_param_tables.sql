CREATE TABLE IF NOT EXISTS emprego_t_cefp (
    id SERIAL PRIMARY KEY,
    denominacao VARCHAR(550),
    sigla VARCHAR(25),
    telefone VARCHAR(50),
    telemovel VARCHAR(50),
    url_site VARCHAR(150),
    organization_id INTEGER,
    email VARCHAR(255),
    ilha VARCHAR(50),
    concelho VARCHAR(50),
    zona VARCHAR(50),
    endereco VARCHAR(550),
    area_abrangida JSONB,
    dm_estado VARCHAR(25),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(150),
    date_update TIMESTAMP,
    user_update VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS emprego_t_tecnicos (
    id SERIAL PRIMARY KEY,
    denominacao VARCHAR(550),
    tipo_tecnico VARCHAR(25),
    telefone VARCHAR(50),
    cefp_id INTEGER,
    telemovel VARCHAR(50),
    email VARCHAR(255),
    ilha VARCHAR(50),
    concelho VARCHAR(50),
    zona VARCHAR(50),
    endereco VARCHAR(550),
    dm_estado VARCHAR(55),
    user_associado INTEGER,
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(150),
    date_update TIMESTAMP,
    user_update VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS emprego_t_requisito (
    id SERIAL PRIMARY KEY,
    requisito VARCHAR(550),
    tipo_servico VARCHAR(255),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(150),
    dm_estado VARCHAR(55),
    date_update TIMESTAMP,
    user_update VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS emprego_t_param_report (
    id SERIAL PRIMARY KEY,
    logotipo_iefp VARCHAR(555),
    logotipo_dge VARCHAR(555),
    date_create TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    user_create VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS emprego_t_tipo_documento_param (
    id SERIAL PRIMARY KEY,
    formulario_referente VARCHAR(255),
    obrigatoriedade VARCHAR(25),
    status VARCHAR(255),
    tipo_documento INTEGER,
    tipo_documento_desc VARCHAR(255),
    user_create INTEGER,
    user_update INTEGER,
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_update TIMESTAMP
);

CREATE TABLE IF NOT EXISTS emprego_t_cefp_areas_abrangidas (
    id SERIAL PRIMARY KEY,
    ilha VARCHAR(255),
    concelho VARCHAR(25),
    cefp_id INTEGER,
    status VARCHAR(25),
    user_create INTEGER,
    user_update INTEGER,
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_update TIMESTAMP
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_emprego_t_tecnicos_cefp'
    ) THEN
        ALTER TABLE emprego_t_tecnicos
            ADD CONSTRAINT fk_emprego_t_tecnicos_cefp
            FOREIGN KEY (cefp_id)
            REFERENCES emprego_t_cefp(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_emprego_t_cefp_areas_abrangidas_cefp'
    ) THEN
        ALTER TABLE emprego_t_cefp_areas_abrangidas
            ADD CONSTRAINT fk_emprego_t_cefp_areas_abrangidas_cefp
            FOREIGN KEY (cefp_id)
            REFERENCES emprego_t_cefp(id);
    END IF;
END $$;