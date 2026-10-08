-- ============================================================================
-- PROJETO: CENTRAL DE INFORMACOES DO SECTOR (DGE) - POWER BI
-- MODULO: INTERMEDIACAO LABORAL (dge-api-intermed-lab)
-- SCRIPT: UPDATE / RECRIACAO APENAS DAS VISTAS AFETADAS
-- DATA: 2026-10-08
-- ============================================================================
-- BASE DE DESTINO: db_emprego_bi
--
-- Executar este script caso o ambiente ja tenha sido configurado previamente
-- (com extensoes, foreign tables e procedure criadas).
--
-- ALTERACOES INCLUIDAS:
--   1. vw_acolhimento:
--      - Removidas colunas: nome, sexo, origem_colocacao
--      - Mantida habilitacao_literaria (historica por acolhimento)
--      - estado_entrevista com COALESCE(ent.dm_status_entrevista, a.status_entrevista)
--      - Adicionado indice idx_mv_acolhimento_habilitacao
--   2. vw_utente:
--      - Adicionada coluna 'cefp' (e 'cefp_sigla') do primeiro acolhimento (data mais antiga)
--      - Removidas colunas: habilitacao_literaria, total_acolhimentos, ultimo_acolhimento, servico_mais_solicitado
--      - Adicionado indice idx_mv_utente_cefp
--   3. vw_entrevista:
--      - estado_entrevista com COALESCE(e.dm_status_entrevista, a.status_entrevista)
-- ============================================================================


-- ============================================================================
-- 1. VISTA DE ACOLHIMENTOS  ->  mv_acolhimento / vw_acolhimento
-- ============================================================================
DROP VIEW IF EXISTS public.vw_acolhimento CASCADE;
DROP MATERIALIZED VIEW IF EXISTS public.mv_acolhimento CASCADE; 

CREATE MATERIALIZED VIEW public.mv_acolhimento AS
SELECT
    -- identidade
    a.id                                       AS id,
    a.id_pessoa                                AS pessoa_id,
    a.id_utente                                AS id_utente,
    a.num_inscricao                            AS codigo_acolhimento,
    COALESCE(
        a.detalhes->>'habilitacaoLiteraria',
        a.detalhes->>'habilitacao_literaria',
        u.habilitacao_literaria
    )                                          AS habilitacao_literaria,
    -- data do acolhimento
    a.date_create                              AS data,
    EXTRACT(YEAR FROM a.date_create)::int      AS ano,
    to_char(a.date_create, 'TMMonth')          AS mes,
    -- classificacao
    a.tipo_servico                             AS tipo_servico,
    a.tipo_servico_desc                        AS tipo_servico_desc,
    a.tipo_utente                              AS tipo_utente,
    a.tipo_utente_desc                         AS tipo_utente_desc,
    a.canal                                    AS canal,
    a.canal_desc                               AS canal_desc,
    a.fonte_informacao                         AS fonte_informacao,
    a.tecnico_atendimento                      AS tecnico,
    -- territorial / cefp
    c.denominacao                              AS cefp,
    c.sigla                                    AS cefp_sigla,
    c.ilha                                     AS ilha,
    c.concelho                                 AS concelho,
    -- encaminhamento (AVISO 2b)
    enc.data_encaminhamento                    AS data_encaminhamento,
    -- entrevista (AVISO 2c)
    CASE WHEN ent.id IS NULL THEN 'nao' ELSE 'sim' END
                                            AS entrevista_agendada,
    ent.data_agendamento_entrevista            AS data_agendamento_entrevista,
    ent.data_entrevista                        AS data_realizacao_entrevista,
    COALESCE(ent.dm_status_entrevista, a.status_entrevista)
                                            AS estado_entrevista,
    ent.parecer_io                             AS parecer_io,
    dom.description                           AS parecer_io_desc,
    ent.obs_parecer_io                         AS obs_parecer_io,
    -- sessao de balanco (AVISO 2e)
    CASE WHEN bal.id IS NULL THEN 'nao' ELSE 'sim' END
                                            AS sessao_balanco,
    bal.data_agendamento_balanco               AS data_agendamento_balanco,
    bal.data_realizacao_balanco                AS data_realizacao_balanco,
    bal.tipo_balanco                           AS tipo_balanco,
    bal.estado_balanco                         AS estado_balanco,
    -- colocacao (AVISO 2f e AVISO 3)
    CASE UPPER(TRIM(COALESCE(a.tipo_servico, '')))
        WHEN 'EMPREGO'  THEN COALESCE(col_pac.data, col_emp.data)
        WHEN 'FORMACAO' THEN COALESCE(col_sgf.data, col_emp.data)
        ELSE                 col_emp.data
    END                                        AS data_colocacao
FROM public.emprego_t_detalhes_acolhimento a
LEFT JOIN LATERAL (
    SELECT ut.habilitacao_literaria
    FROM public.emprego_t_utente ut
    WHERE ut.id = a.id_utente
       OR (a.id_utente IS NULL AND ut.pessoa_id = a.id_pessoa)
    ORDER BY (ut.id = a.id_utente) DESC NULLS LAST, ut.id
    LIMIT 1
) u ON TRUE
LEFT JOIN public.emprego_t_cefp c
    ON c.id = a.cefp_id
LEFT JOIN LATERAL (
    SELECT s.date_create AS data_encaminhamento
    FROM public.emprego_t_acolhimento_servico s
    WHERE s.id_acolhimento = a.id
    ORDER BY s.date_create ASC NULLS LAST, s.id
    LIMIT 1
) enc ON TRUE
LEFT JOIN LATERAL (
    SELECT e.id,
           e.date_create        AS data_agendamento_entrevista,
           e.data_entrevista,
           e.dm_status_entrevista,
           e.parecer_io,
           e.obs_parecer_io
    FROM public.emprego_t_agendamento_entrevista e
    WHERE e.id_acolhimento = a.id
    ORDER BY e.date_create DESC NULLS LAST, e.id DESC
    LIMIT 1
) ent ON TRUE
LEFT JOIN LATERAL (
    SELECT d.description
    FROM public.tbl_domain d
    JOIN public.tbl_env env ON env.id = d.env_fk
    WHERE UPPER(TRIM(d.dominio)) = 'PARECER_ENTREVISTA'
      AND LOWER(TRIM(env.dad)) = 'interm_laboral'
      AND UPPER(TRIM(d.valor)) = UPPER(TRIM(ent.parecer_io))
    LIMIT 1
) dom ON TRUE
LEFT JOIN LATERAL (
    SELECT b.id,
           b.date_create AS data_agendamento_balanco,
           b.data        AS data_realizacao_balanco,
           b.tipo_balanco,
           b.dm_status   AS estado_balanco
    FROM public.emprego_t_agendamento_balanco b
    WHERE b.entrevista_id = ent.id
    ORDER BY b.date_create DESC NULLS LAST, b.id DESC
    LIMIT 1
) bal ON TRUE
LEFT JOIN LATERAL (
    SELECT MIN(pac.date_create) AS data
    FROM public.pac_t_colaborador_contratado pac
    WHERE UPPER(TRIM(COALESCE(a.tipo_servico, ''))) = 'EMPREGO'
      AND pac.pessoa_id = a.id_pessoa
      AND pac.date_create > a.date_create
) col_pac ON TRUE
LEFT JOIN LATERAL (
    SELECT MIN(cs.date_create) AS data
    FROM public.sgf_t_candidato_selecionados cs
    JOIN public.sgf_t_candidato sc ON sc.id = cs.candidato_id
    WHERE UPPER(TRIM(COALESCE(a.tipo_servico, ''))) = 'FORMACAO'
      AND sc.pessoa_id = a.id_pessoa
      AND cs.date_create > a.date_create
) col_sgf ON TRUE
LEFT JOIN LATERAL (
    SELECT MIN(cc.date_create) AS data
    FROM public.emprego_t_colocacao_candidato cc
    WHERE UPPER(TRIM(COALESCE(a.tipo_servico, ''))) <> 'FORMACAO'
      AND cc.pessoa_id = a.id_pessoa
      AND cc.date_create > a.date_create
) col_emp ON TRUE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_acolhimento_id
ON public.mv_acolhimento (id);

CREATE INDEX IF NOT EXISTS idx_mv_acolhimento_ano
ON public.mv_acolhimento (ano);

CREATE INDEX IF NOT EXISTS idx_mv_acolhimento_servico
ON public.mv_acolhimento (tipo_servico);

CREATE INDEX IF NOT EXISTS idx_mv_acolhimento_cefp
ON public.mv_acolhimento (cefp);

CREATE INDEX IF NOT EXISTS idx_mv_acolhimento_pessoa
ON public.mv_acolhimento (pessoa_id);

CREATE INDEX IF NOT EXISTS idx_mv_acolhimento_entrevista
ON public.mv_acolhimento (entrevista_agendada);

CREATE INDEX IF NOT EXISTS idx_mv_acolhimento_habilitacao
ON public.mv_acolhimento (habilitacao_literaria);

CREATE OR REPLACE VIEW public.vw_acolhimento AS
SELECT * FROM public.mv_acolhimento;


-- ============================================================================
-- 2. VISTA DE UTENTES  ->  mv_utente / vw_utente
-- ============================================================================
DROP VIEW IF EXISTS public.vw_utente CASCADE;
DROP MATERIALIZED VIEW IF EXISTS public.mv_utente CASCADE;

CREATE MATERIALIZED VIEW public.mv_utente AS
SELECT
    u.id                                       AS id,
    u.pessoa_id                                AS pessoa_id,
    u.nome                                     AS nome,
    u.sexo                                     AS sexo,
    u.data_nascimento                          AS data_nascimento,
    u.tipo_documento                           AS tipo_documento,
    u.num_documento                            AS num_documento,
    u.date_create                              AS data_registo,
    EXTRACT(YEAR FROM u.date_create)::int      AS ano,
    to_char(u.date_create, 'TMMonth')          AS mes,
    -- CEFP do primeiro acolhimento (centro onde fez o registo inicial)
    prim_ac.cefp                               AS cefp,
    prim_ac.cefp_sigla                         AS cefp_sigla
FROM public.emprego_t_utente u
LEFT JOIN LATERAL (
    -- Primeiro acolhimento do utente (considerando a data mais antiga)
    -- para associar o CEFP de acolhimento inicial / origem do utente.
    SELECT c.denominacao AS cefp,
           c.sigla       AS cefp_sigla
    FROM public.emprego_t_detalhes_acolhimento a
    LEFT JOIN public.emprego_t_cefp c ON c.id = a.cefp_id
    WHERE a.id_utente = u.id
       OR (a.id_utente IS NULL AND a.id_pessoa = u.pessoa_id)
    ORDER BY a.date_create ASC NULLS LAST, a.id ASC
    LIMIT 1
) prim_ac ON TRUE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_utente_id
ON public.mv_utente (id);

CREATE INDEX IF NOT EXISTS idx_mv_utente_ano
ON public.mv_utente (ano);

CREATE INDEX IF NOT EXISTS idx_mv_utente_sexo
ON public.mv_utente (sexo);

CREATE INDEX IF NOT EXISTS idx_mv_utente_cefp
ON public.mv_utente (cefp);

CREATE OR REPLACE VIEW public.vw_utente AS
SELECT * FROM public.mv_utente;


-- ============================================================================
-- 3. VISTA DE ENTREVISTAS  ->  mv_entrevista / vw_entrevista
-- ============================================================================
DROP VIEW IF EXISTS public.vw_entrevista CASCADE;
DROP MATERIALIZED VIEW IF EXISTS public.mv_entrevista CASCADE;

CREATE MATERIALIZED VIEW public.mv_entrevista AS
SELECT
    e.id                                       AS id,
    e.id_acolhimento                           AS id_acolhimento,
    a.num_inscricao                            AS codigo_acolhimento,
    a.id_pessoa                                AS pessoa_id,
    u.nome                                     AS nome,
    u.sexo                                     AS sexo,
    a.tipo_servico                             AS tipo_servico,
    a.tipo_servico_desc                        AS tipo_servico_desc,
    c.denominacao                              AS cefp,
    c.ilha                                     AS ilha,
    c.concelho                                 AS concelho,
    e.nome_tecnico                             AS tecnico,
    e.canal                                    AS canal,
    e.local_entrevista                         AS local_entrevista,
    e.date_create                              AS data_agendamento,
    EXTRACT(YEAR FROM e.date_create)::int      AS ano_agendamento,
    to_char(e.date_create, 'TMMonth')          AS mes_agendamento,
    e.data_entrevista                          AS data_realizacao,
    COALESCE(e.dm_status_entrevista, a.status_entrevista)
                                            AS estado_entrevista,
    e.parecer_io                               AS parecer_io,
    dom.description                           AS parecer_io_desc,
    e.obs_parecer_io                           AS obs_parecer_io,
    CASE WHEN bal.id IS NULL THEN 'nao' ELSE 'sim' END
                                            AS sessao_balanco,
    bal.data_balanco                           AS data_balanco,
    bal.tipo_balanco                           AS tipo_balanco,
    bal.estado_balanco                         AS estado_balanco
FROM public.emprego_t_agendamento_entrevista e
LEFT JOIN public.emprego_t_detalhes_acolhimento a
    ON a.id = e.id_acolhimento
LEFT JOIN public.emprego_t_utente u
    ON u.id = e.id_utente
LEFT JOIN public.emprego_t_cefp c
    ON c.id = COALESCE(e.id_cefp, a.cefp_id)
LEFT JOIN LATERAL (
    SELECT d.description
    FROM public.tbl_domain d
    JOIN public.tbl_env env ON env.id = d.env_fk
    WHERE UPPER(TRIM(d.dominio)) = 'PARECER_ENTREVISTA'
      AND LOWER(TRIM(env.dad)) = 'interm_laboral'
      AND UPPER(TRIM(d.valor)) = UPPER(TRIM(e.parecer_io))
    LIMIT 1
) dom ON TRUE
LEFT JOIN LATERAL (
    SELECT b.id,
           b.data        AS data_balanco,
           b.tipo_balanco,
           b.dm_status   AS estado_balanco
    FROM public.emprego_t_agendamento_balanco b
    WHERE b.entrevista_id = e.id
    ORDER BY b.date_create DESC NULLS LAST, b.id DESC
    LIMIT 1
) bal ON TRUE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_entrevista_id
ON public.mv_entrevista (id);

CREATE INDEX IF NOT EXISTS idx_mv_entrevista_ano
ON public.mv_entrevista (ano_agendamento);

CREATE INDEX IF NOT EXISTS idx_mv_entrevista_estado
ON public.mv_entrevista (estado_entrevista);

CREATE INDEX IF NOT EXISTS idx_mv_entrevista_cefp
ON public.mv_entrevista (cefp);

CREATE OR REPLACE VIEW public.vw_entrevista AS
SELECT * FROM public.mv_entrevista;


-- ============================================================================
-- 4. REFRESH E VALIDACAO
-- ============================================================================
CALL sp_refresh_emprego_bi();

-- Validacao rapida das contagens
SELECT
    (SELECT COUNT(*) FROM public.vw_utente)       AS total_utentes,
    (SELECT COUNT(*) FROM public.vw_acolhimento)  AS total_acolhimentos,
    (SELECT COUNT(*) FROM public.vw_entrevista)   AS total_entrevistas;

-- Amostra de acolhimento (sem nome, sexo, origem_colocacao; com habilitacao)
SELECT id, pessoa_id, id_utente, codigo_acolhimento, habilitacao_literaria, data, tipo_servico, cefp
FROM public.vw_acolhimento
LIMIT 5;

-- Amostra de utentes (sem habilitacao_literaria; com cefp)
SELECT id, nome, sexo, data_registo, cefp, cefp_sigla
FROM public.vw_utente
LIMIT 5;
