-- ============================================================================
-- PROJETO: CENTRAL DE INFORMACOES DO SECTOR (DGE) - POWER BI
-- MODULO: INTERMEDIACAO LABORAL (dge-api-intermed-lab)
--         Acolhimento / Atendimento e Orientacao Profissional
-- SCRIPT: Vistas de BI (acolhimento, utentes e entrevistas)
-- FONTE: Especificacao Tecnica "Vistas que devem ser criadas"
--        (CI - Dashboards Acolhimento e Orientacao Profissional)
-- DOC: CI_Especificacao_Tecnica_Vistas_Acolhimento_Orientacao.pptx
-- DATA: 2026-09-28
-- ============================================================================
--
-- BASE DE DESTINO: db_emprego_bi  (base de BI, NAO a base de negocio)
--
-- ###########################################################################
-- # EXECUCAO MANUAL - o Flyway NAO corre este script.                        #
-- #                                                                          #
-- # A aplicacao dge-api-intermed-lab nao tem nenhuma configuracao que        #
-- # apanhe este ficheiro (o Flyway so le classpath:db/migration), portanto   #
-- # o arranque da app nao executa nada aqui. O script e aplicado a mao.      #
-- #                                                                          #
-- # REEXECUTAVEL: os IMPORT FOREIGN SCHEMA sao protegidos por verificacoes   #
-- # de existencia e todas as vistas usam IF EXISTS / DROP. O script pode ser #
-- # executado novamente sem recriar as foreign tables ja existentes.         #
-- ###########################################################################
--
-- A base de negocio NAO e tocada. Todas as tabelas de origem sao ligadas aqui
-- por postgres_fdw. O Power BI liga-se apenas a db_emprego_bi.
--
-- PRE-REQUISITO (uma vez, a mao - nem o psql nem o Flyway criam bases):
--     CREATE DATABASE db_emprego_bi;
-- O utilizador tem de ser superuser (CREATE EXTENSION e CREATE SERVER exigem
-- privilegio).
--
-- ###########################################################################
-- # AVISO 1 - NOMES REAIS DAS BASES (verificados no servidor em 2026-09-28)  #
-- #                                                                          #
-- #   emprego_server  ->  bd_dge_emprego     (COM "bd_", nao "db_")        #
-- #   igrp_server     ->  db_igrp_dge1       (COM "1" no fim)               #
-- #   pac_server      ->  db_dge_pac                                       #
-- #   sgf_server      ->  db_dge_sgf                                       #
-- #                                                                          #
-- # CUIDADO: existem duas bases IGRP quase com o mesmo nome.                 #
-- #     db_igrp_dge   -> NAO tem o ambiente 'interm_laboral'. Nao serve.      #
-- #     db_igrp_dge1  -> tem 'interm_laboral' com PARECER_ENTREVISTA         #
-- #                       (APROVAR/Aprovar, RECUSAR/Recusar) e                #
-- #                       ESTADO_ENTREVISTA (PENDENTE, REALIZADO). Esta e     #
-- #                       a que este script usa.                             #
-- #                                                                          #
-- # NOTA: o script de BI do modulo RVCC aponta para 'db_dge_emprego' e        #
-- # 'db_igrp_dge'. Nenhuma das duas existe com esse nome neste servidor.     #
-- ###########################################################################
--
--     ORIGEM (postgres_fdw)                |  TABELAS
--     --------------------------------------|------------------------------
--     emprego_server  -> bd_dge_emprego    |  emprego_t_detalhes_acolhimento
--                                        |  emprego_t_utente
--                                        |  emprego_t_cefp
--                                        |  emprego_t_agendamento_entrevista
--                                        |  emprego_t_agendamento_balanco
--                                        |  emprego_t_acolhimento_servico
--                                        |  emprego_t_colocacao_candidato
--     igrp_server     -> db_igrp_dge1      |  tbl_domain
--                                        |  tbl_env
--     pac_server      -> db_dge_pac        |  pac_t_colaborador_contratado
--     sgf_server      -> db_dge_sgf        |  sgf_t_candidato
--                                        |  sgf_t_candidato_selecionados
--
--     TOTAL: 12 foreign tables
--
-- COLUNAS USADAS (nomes reais, confirmados com information_schema)
--     tbl_domain  : id, description, domain_type, dominio, ordem, status,
--                   valor, env_fk            <-- "description", nao "descricao"
--     tbl_env     : id, dad, description, name, status, ...
--     employment_t_detalhes_acolhimento    : ver secao 2
--     emprego_t_agendamento_entrevista     : ..., parecer_io, obs_parecer_io
--     emprego_t_agendamento_balanco        : ..., entrevista_id, tipo_balanco
-- ###########################################################################
--
-- ###########################################################################
-- # AVISO 2 - DIVERGENCIAS ENTRE A ESPECIFICACAO E A BASE DE DADOS            #
-- #                                                                          #
-- #  Documento                                        ->  Origem correcta   #
-- #  ------------------------------                    ------------------- #
-- #  ...acolhimento.parecer_io                        ->  emprego_t_       #
-- #                                                       agendamento_    #
-- #                                                       entrevista       #
-- #                                                       .parecer_io      #
-- #  ...acolhimento.data_encaminhamento               ->  emprego_t_       #
-- #                                                       acolhimento_     #
-- #                                                       servico          #
-- #                                                       .date_create     #
-- #  agendamento_entrevista.data_encaminhamento       ->  (coluna NAO      #
-- #                                                       existe)         #
-- #  dominio 'parecer_io'                             ->  dominio         #
-- #                                                       'PARECER_ENTREVISTA'
-- #  tbl_dominio.valor                                ->  db_igrp_dge1    #
-- #                                                       .tbl_domain      #
-- #                                                       (+ tbl_env)      #
-- #  sessao_balanco / data_agendamento_balanco         ->  emprego_t_      #
-- #                                                       agendamento_     #
-- #                                                       balanco          #
-- #  ...colocacao_candidato.date_create maior que     ->  maior que       #
-- #  ...colocacao_candidato.date_create (auto-)          a.date_create   #
-- ###########################################################################
--
-- DETALHE DE CADA DIVERGENCIA
--
-- (a) parecer_io / parecer - o parecer de IO e do AGENDAMENTO DE ENTREVISTA,
--     nao do acolhimento. A coluna nao existe em emprego_t_detalhes_acolhimento.
--     A view traz o parecer da entrevista mais recente do acolhimento.
--
-- (b) data_encaminhamento - nao existe em emprego_t_detalhes_acolhimento.
--     Substituida por emprego_t_acolhimento_servico.date_create, que e a data
--     em que o encaminhamento do utente para o servico foi registado. E o
--     unico registo que data o encaminhamento no modulo.
--     Alimenta o indicador "Tempo medio de acolhimento ate ao encaminhamento".
--
-- (c) entrevista_agendada - o documento pede agendamento_entrevista
--     .data_encaminhamento, coluna que nao existe. A view expoe 'sim'/'nao'
--     conforme exista ou nao um agendamento ligado ao acolhimento. A data esta
--     em data_agendamento_entrevista (agendamento_entrevista.date_create).
--
-- (d) parecer_io_desc - nao existe nenhuma "tbl_dominio" neste modulo. Os
--     dominios vem de db_igrp_dge1 (IGRP), como em IgrpDominioRepository.java:
--         tbl_domain JOIN tbl_env ON tbl_env.id = tbl_domain.env_fk
--         WHERE dominio = ... AND tbl_env.dad = 'interm_laboral'
--     O dominio do parecer e 'PARECER_ENTREVISTA' (EmpregoDominio.java), nao
--     'parecer_io'. O mesmo mecanismo enriquece estado_entrevista com
--     'ESTADO_ENTREVISTA' na view de entrevistas.
--
-- (e) sessao_balanco / data_agendamento_balanco - o documento nao indica a
--     fonte. A ligacao correcta e entrevista -> balanco
--     (emprego_t_agendamento_balanco.entrevista_id). A view expoe tambem
--     data_realizacao_balanco, tipo_balanco e estado_balanco.
--     NAO existe fallback por utente: com poucos utentes e muitos
--     acolhimentos por utente, o fallback repetia a mesma sessao de balanco em
--     12 dos 28 acolhimentos. Ver o comentario no SQL.
--
-- (f) data_colocacao - o documento tem um erro de escrita: "se
--     emprego_t_colocacao_candidato.date_create e maior do que
--     emprego_t_colocacao_candidato.date_create" (a mesma coluna dos dois
--     lados). Corrigido para "maior do que emprego_t_detalhes_acolhimento
--     .date_create", que e a unica leitura faz sentido. Onde ha mais do que
--     uma colocacao posterior ao acolhimento, a view fica com a MAIS ANTIGA
--     (MIN) - e a que da o "tempo medio ate a colocacao".
--
-- ###########################################################################
--
-- ###########################################################################
-- # AVISO 3 - tipo_servico                                                   #
-- #                                                                          #
-- # A especificacao preve EMPREGO / PEPE / FORMACAO. Os valores que existem  #
-- # hoje em emprego_t_detalhes_acolhimento sao outros (ver validacao 6.4).   #
-- # O mapeamento aplicado na view:                                           #
-- #                                                                          #
-- #   tipo_servico        ->  origem de data_colocacao                      #
-- #                                                                          #
--   EMPREGO              ->  pac_t_colaborador_contratado                 #
--                           (fallback: emprego_t_colocacao_candidato)       #
--   FORMACAO             ->  sgf_t_candidato_selecionados                  #
--                           (fallback: emprego_t_colocacao_candidato)       #
--   PEPE                 ->  emprego_t_colocacao_candidato                #
--   qualquer outro       ->  emprego_t_colocacao_candidato                #
--                           (ex.: SUB_DESEMP)                              #
--                                                                          #
-- # A busca de data_colocacao segue essa prioridade de tabelas conforme o tipo.#
-- #                                                                          #
-- # >>> DADO SUJO: 'SUB_DESEMP' esta gravado com um ESPACO DE ALTURA ZERO     #
-- #     (U+200B, bytes e2 80 8b) no fim da string.                            #
-- #         hex: 5355425f444553454d50 e2808b                                 #
-- #         length('SUB_DESEMP') = 11   (sao 10 caracteres + o invisivel)    #
-- #     Ou seja: um filtro do Power BI por 'SUB_DESEMP' escrito a mao NAO      #
-- #     casa com o dado. A view nao limpa o valor (fiel a origem), por isso    #
-- #     o dashboard tem de usar TRIM/REPLACE ou o script tem de limpar a      #
-- #     origem:  UPDATE emprego_t_detalhes_acolhimento                         #
-- #               SET tipo_servico = 'SUB_DESEMP'                             #
-- #             WHERE tipo_servico LIKE 'SUB_DESEMP%';                        #
-- ###########################################################################
--
-- ###########################################################################
-- # AVISO 4 - GRAO E QUALIDADE DOS DADOS                                    #
-- #                                                                          #
-- # A view vw_acolhimento tem 1 linha por ACOLHIMENTO.                      #
-- #   - Um utente pode ter VARIOS acolhimentos: ok, sao linhas diferentes.  #
-- #   - Um acolhimento pode ter VARIAS entrevistas: a view fica com a mais   #
--     recente (LATERAL ... ORDER BY date_create DESC LIMIT 1). Para contar   #
--     todas as entrevistas use vw_entrevista.                               #
--   - Um acolhimento pode ter VARIOS balancos: idem, o mais recente.        #
-- #   - sgf_t_candidato.pessoa_id e INTEGER e emprego_t_colocacao_candidato #
--     .pessoa_id e BIGINT: as comparacoes fazem cast implicito sem perda.   #
-- #   - id_utente e a FK real para emprego_t_utente. O documento manda usar  #
--     utente.pessoa_id = acolhimento.id_pessoa. A view prefere o id_utente   #
--     (ligacao exata, sem ambiguidade) e so cai para pessoa_id quando o      #
--     id_utente e nulo. Ver validacao 7.5 para medir a divergencia.           #
-- ###########################################################################
--
-- ###########################################################################
-- # AVISO 5 - VOLUME DOS DADOS ACTUAIS (contagens de 2026-09-28)              #
-- #                                                                          #
-- #   emprego_t_detalhes_acolhimento .......  28 registos                    #
-- #   emprego_t_utente ....................   3 registos                    #
-- #   emprego_t_agendamento_entrevista .....   1 registo                     #
-- #   emprego_t_acolhimento_servico ........   1 registo                     #
-- #   emprego_t_agendamento_balanco ........   1 registo                     #
-- #   emprego_t_colocacao_candidato ........   2 registos                    #
-- #                                                                          #
-- # => Quase todos os indicadores das folhas "Encaminhamento e Entrevistas"    #
-- #    e "Acolhimento / Atendimento" vao mostrar 0 ou valores unicos, porque   #
-- #    so ha 1 entrevista e 1 sessao de balanco em todo o historico.           #
-- #    As views estao correctas; o que falta e volume de dados.               #
-- #                                                                          #
-- # Os 28 acolhimentos apontam para apenas 3 utentes, e tipo_servico esta      #
-- # muito concentrado: FORMACAO 13, EMPREGO 10, SUB_DESEMP 4, NULL 1.         #
-- ###########################################################################
--
-- ###########################################################################
-- # AVISO 6 - OS CODIGOS GRAVADOS NAO BATEM COM OS DOMINIOS DO IGRP           #
-- #                                                                          #
-- # As colunas de dominio estao gravadas com codigos que NAO existem no        #
-- # IGRP. O resultado e que parecer_io_desc e a descricao de                   #
-- # estado_entrevista vao vir a NULL. A view nao inventa nem adivinha nada.     #
-- #                                                                          #
-- #   dado gravado                        dominio IGRP ('interm_laboral')      #
-- #   ------------------------------       -------------------------------      #
-- #   parecer_io        = '1'              PARECER_ENTREVISTA = APROVAR /      #
-- #                                                     RECUSAR               #
-- #   dm_status_entrevista = 'EEE'         ESTADO_ENTREVISTA  = PENDENTE /     #
-- #                                                     REALIZADO            #
-- #                                                                          #
-- # Confirmação (ver validacao 7.6):                                        #
-- #     com_parecer = 1, com_descricao = 0                                   #
-- #                                                                          #
-- # Para o dashboard mostrar as descricoes, a origem tem de gravar APROVAR /   #
-- # RECUSAR e PENDENTE / REALIZADO.                                            #
-- #                                                                          #
-- # NOTA: emprego_t_detalhes_acolhimento.status_entrevista e um campo          #
-- # DIFERENTE e tem PENDENTE / REALIZADO / AGENDADO (e um deles gravado com     #
-- # quebras de linha a frente). A view nao o expoe, para nao confundir com     #
-- # estado_entrevista, que vem da entrevista.                                  #
-- ###########################################################################
--
-- ###########################################################################
-- # AVISO 7 - tipo_servico_desc ESTA DESALINHADO COM tipo_servico              #
-- #                                                                          #
-- # A view expoe as duas colunas como a origem as tem. Nao ha forma de as      #
-- # corrigir aqui, e o mesmo happens no modulo RVCC.                          #
-- #                                                                          #
-- #   tipo_servico      tipo_servico_desc    registos                         #
-- #   ---------------   ----------------    ---------                        #
-- #   FORMACAO          PEPE                        8                         #
-- #   EMPREGO           (vazio)                     5                         #
-- #   SUB_DESEMP        SUB_DESEMP                 4                         #
-- #   EMPREGO           ATENDIMENTO                 2                         #
-- #   FORMACAO          EMPREGO                     2                         #
-- #   EMPREGO           GERME                       1                         #
-- #   FORMACAO          GERME                       1                         #
-- #   EMPREGO           RVCC                        1                         #
-- #   FORMACAO          ATENDIMENTO                 1                         #
-- #   EMPREGO           FORMACAO                    1                         #
-- #   FORMACAO          (vazio)                     1                         #
-- #   (vazio)           (vazio)                     1                         #
-- #                                                                          #
-- # (o ZWSP de SUB_DESEMP nao e visivel aqui - ver AVISO 3)                    #
-- #                                                                          #
-- # NUNCA filtrar o dashboard por tipo_servico_desc. Usar tipo_servico, ou     #
-- # corrigir a origem:                                                         #
-- #   UPDATE emprego_t_detalhes_acolhimento a                                  #
-- #      SET tipo_servico_desc = tipo_servico                                 #
-- #    WHERE tipo_servico_desc IS DISTINCT FROM tipo_servico;                   #
-- ###########################################################################
--
-- ###########################################################################
-- # AVISO 8 - tecnico_atendimento MISTURA PESSOAS E ENTIDADES                 #
-- #                                                                          #
-- # Os 4 valores distintos em emprego_t_detalhes_acolhimento.tecnico_atendimento:
-- #     "Joao Fernandes"                                       <- tecnico     #
-- #     "Tecnico Teste"                                        <- tecnico     #
-- #     "Nositeste"                                             <- sem sentido #
-- #     "Criacao e venda de produtos e servicos turisticos"     <- NOME DE     #
-- #                                                                ENTIDADE    #
-- #                                                                          #
-- # A view expoe a coluna tal e qual (a especificacao pede                      #
-- # "Total de acolhimento por tecnico"). Para separar, ha de se cruzar com     #
-- # emprego_t_tecnicos.denominacao ou com emprego_t_cefp.denominacao.          #
-- ###########################################################################
-- ============================================================================


-- ============================================================================
-- 1. LIGACAO AS BASES DE ORIGEM (postgres_fdw)
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- ---------------------------------------------------------------------------
-- 1.1. Base de negocio da intermediacao laboral
-- ---------------------------------------------------------------------------
CREATE SERVER IF NOT EXISTS emprego_server
FOREIGN DATA WRAPPER postgres_fdw
OPTIONS (host 'postgres16sta', dbname 'db_dge_emprego', port '5432');

CREATE USER MAPPING IF NOT EXISTS FOR postgres
SERVER emprego_server
OPTIONS (user 'postgres', password 'jkdsht98YYyewbhcb');

DO $$
BEGIN
    IF to_regclass('public.emprego_t_detalhes_acolhimento') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (emprego_t_detalhes_acolhimento)
        FROM SERVER emprego_server INTO public;
    END IF;

    IF to_regclass('public.emprego_t_utente') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (emprego_t_utente)
        FROM SERVER emprego_server INTO public;
    END IF;

    IF to_regclass('public.emprego_t_cefp') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (emprego_t_cefp)
        FROM SERVER emprego_server INTO public;
    END IF;

    IF to_regclass('public.emprego_t_agendamento_entrevista') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (emprego_t_agendamento_entrevista)
        FROM SERVER emprego_server INTO public;
    ELSE
        ALTER FOREIGN TABLE public.emprego_t_agendamento_entrevista
            ADD COLUMN IF NOT EXISTS data_encaminhamento DATE;
    END IF;

    IF to_regclass('public.emprego_t_agendamento_balanco') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (emprego_t_agendamento_balanco)
        FROM SERVER emprego_server INTO public;
    END IF;

    IF to_regclass('public.emprego_t_acolhimento_servico') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (emprego_t_acolhimento_servico)
        FROM SERVER emprego_server INTO public;
    END IF;

    IF to_regclass('public.emprego_t_colocacao_candidato') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (emprego_t_colocacao_candidato)
        FROM SERVER emprego_server INTO public;
    END IF;
END
$$;

-- ---------------------------------------------------------------------------
-- 1.2. IGRP - dominios (descricoes de parecer_io, estado_entrevista, ...)
--     ATENCAO: db_igrp_dge1, nao db_igrp_dge. Ver AVISO 1.
-- ---------------------------------------------------------------------------
CREATE SERVER IF NOT EXISTS igrp_server
FOREIGN DATA WRAPPER postgres_fdw
OPTIONS (host 'postgres16sta', dbname 'db_igrp_dge', port '5432');

CREATE USER MAPPING IF NOT EXISTS FOR postgres
SERVER igrp_server
OPTIONS (user 'postgres', password 'jkdsht98YYyewbhcb');

DO $$
BEGIN
    IF to_regclass('public.tbl_domain') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (tbl_domain)
        FROM SERVER igrp_server INTO public;
    END IF;

    IF to_regclass('public.tbl_env') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (tbl_env)
        FROM SERVER igrp_server INTO public;
    END IF;
END
$$;

-- ---------------------------------------------------------------------------
-- 1.3. Modulo PAC - contratacao (data_colocacao para tipo_servico = EMPREGO)
-- ---------------------------------------------------------------------------
CREATE SERVER IF NOT EXISTS pac_server
FOREIGN DATA WRAPPER postgres_fdw
OPTIONS (host 'postgres16sta', dbname 'db_dge_pac', port '5432');

CREATE USER MAPPING IF NOT EXISTS FOR postgres
SERVER pac_server
OPTIONS (user 'postgres', password 'jkdsht98YYyewbhcb');

DO $$
BEGIN
    IF to_regclass('public.pac_t_colaborador_contratado') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (pac_t_colaborador_contratado)
        FROM SERVER pac_server INTO public;
    END IF;
END
$$;

-- ---------------------------------------------------------------------------
-- 1.4. Modulo SGF - selecao de candidatos (data_colocacao para FORMACAO)
-- ---------------------------------------------------------------------------
CREATE SERVER IF NOT EXISTS sgf_server
FOREIGN DATA WRAPPER postgres_fdw
OPTIONS (host 'postgres16sta', dbname 'db_dge_sgf', port '5432');

CREATE USER MAPPING IF NOT EXISTS FOR postgres
SERVER sgf_server
OPTIONS (user 'postgres', password 'jkdsht98YYyewbhcb');

DO $$
BEGIN
    IF to_regclass('public.sgf_t_candidato') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (sgf_t_candidato)
        FROM SERVER sgf_server INTO public;
    END IF;

    IF to_regclass('public.sgf_t_candidato_selecionados') IS NULL THEN
        IMPORT FOREIGN SCHEMA public LIMIT TO (sgf_t_candidato_selecionados)
        FROM SERVER sgf_server INTO public;
    END IF;
END
$$;


-- ============================================================================
-- 2. VISTA DE ACOLHIMENTOS  ->  mv_acolhimento / vw_acolhimento
--    Grain: 1 linha por acolhimento/atendimento (id de
--            emprego_t_detalhes_acolhimento)
--    Corresponde a "view_acolhimento" da especificacao (slides 6, 7 e 8).
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
    -- encaminhamento (obtido da entrevista agendada, com fallback para acolhimento_servico)
    COALESCE(ent.data_encaminhamento, enc.data_encaminhamento)
                                            AS data_encaminhamento,
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
    -- Encaminhamento do utente para o servico. Nao existe
    -- emprego_t_detalhes_acolhimento.data_encaminhamento (AVISO 2b).
    SELECT s.date_create AS data_encaminhamento
    FROM public.emprego_t_acolhimento_servico s
    WHERE s.id_acolhimento = a.id
    ORDER BY s.date_create ASC NULLS LAST, s.id
    LIMIT 1
) enc ON TRUE
LEFT JOIN LATERAL (
    -- Entrevista mais recente do acolhimento.
    SELECT e.id,
           e.data_encaminhamento,
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
    -- Descricao do parecer a partir do dominio do IGRP (AVISO 2d).
    -- Equivalente ao que a aplicacao faz em IgrpDominioRepository.java.
    SELECT d.description
    FROM public.tbl_domain d
    JOIN public.tbl_env env ON env.id = d.env_fk
    WHERE UPPER(TRIM(d.dominio)) = 'PARECER_ENTREVISTA'
      AND LOWER(TRIM(env.dad)) = 'interm_laboral'
      AND UPPER(TRIM(d.valor)) = UPPER(TRIM(ent.parecer_io))
    LIMIT 1
) dom ON TRUE
LEFT JOIN LATERAL (
    -- Sessao de balanco ligada a entrevista.
    --
    -- NAO se faz fallback para b.id_utente = a.id_utente. Com poucos utentes
    -- e muitos acolhimentos por utente, esse fallback atribui a MESMA sessao
    -- de balanco a todos os acolhimentos do utente (no dado actual, 1 registo
    -- de balanco aparecia em 12 acolhimentos). A ligacao correcta e
    -- entrevista -> balanco; se nao houver entrevista, nao ha balanco.
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
    -- type_servico = EMPREGO -> contratacao registada no modulo PAC.
    SELECT MIN(pac.date_create) AS data
    FROM public.pac_t_colaborador_contratado pac
    WHERE UPPER(TRIM(COALESCE(a.tipo_servico, ''))) = 'EMPREGO'
      AND pac.pessoa_id = a.id_pessoa
      AND pac.date_create > a.date_create
) col_pac ON TRUE
LEFT JOIN LATERAL (
    -- type_servico = FORMACAO -> candidato selecionado no modulo SGF.
    SELECT MIN(cs.date_create) AS data
    FROM public.sgf_t_candidato_selecionados cs
    JOIN public.sgf_t_candidato sc ON sc.id = cs.candidato_id
    WHERE UPPER(TRIM(COALESCE(a.tipo_servico, ''))) = 'FORMACAO'
      AND sc.pessoa_id = a.id_pessoa
      AND cs.date_create > a.date_create
) col_sgf ON TRUE
LEFT JOIN LATERAL (
    -- type_servico = PEPE (e restantes) -> colocacao registada neste modulo.
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
-- 3. VISTA DE UTENTES  ->  mv_utente / vw_utente
--    Grain: 1 linha por utente registado (emprego_t_utente.id)
--
--    Necessaria para a folha "Utentes" da especificacao: o KPI "Total de
--    utentes registados" e "Total de utentes por sexo" nao podem ser obtidos
--    de vw_acolhimento, porque um utente registado pode ainda nao ter
--    acolhimento.
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
-- 4. VISTA DE ENTREVISTAS  ->  mv_entrevista / vw_entrevista
--    Grain: 1 linha por sessao de entrevista agendada
--
--    vw_acolhimento so devolve a entrevista mais recente de cada acolhimento.
--    A folha "Encaminhamento e Entrevistas" precisa de TODAS as sessoes
--    ("Total de entrevistas agendadas", "Total de sessoes realizadas",
--    "Total de entrevistas por estado"), por isso existe esta view.
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
    e.data_encaminhamento                      AS data_encaminhamento,
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
-- 5. REFRESH CONCORRENTE, LOG DE EXECUCAO E AGENDAMENTO
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.bi_refresh_log (
    id BIGSERIAL PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL DEFAULT 'INTERMED',
    database_name VARCHAR(100) NOT NULL DEFAULT current_database(),
    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT clock_timestamp(),
    data_fim TIMESTAMP WITH TIME ZONE,
    duracao_segundos NUMERIC(10, 2),
    views_count INTEGER NOT NULL DEFAULT 3,
    status VARCHAR(20) NOT NULL, -- 'EM_EXECUCAO', 'SUCESSO', 'ERRO'
    mensagem_erro TEXT,
    execution_type VARCHAR(20) DEFAULT 'MANUAL' -- 'MANUAL', 'CRON', 'TRIGGER'
);

CREATE INDEX IF NOT EXISTS idx_bi_refresh_log_intermed_data
ON public.bi_refresh_log (data_inicio DESC);

DROP PROCEDURE IF EXISTS sp_refresh_emprego_bi();
DROP PROCEDURE IF EXISTS sp_refresh_emprego_bi(character varying);

CREATE OR REPLACE PROCEDURE sp_refresh_emprego_bi(p_execution_type VARCHAR DEFAULT 'MANUAL')
LANGUAGE plpgsql
AS $$
DECLARE
    v_log_id BIGINT;
    v_start TIMESTAMP WITH TIME ZONE;
    v_end TIMESTAMP WITH TIME ZONE;
    v_duration NUMERIC(10, 2);
    v_err_msg TEXT;
    v_err_detail TEXT;
BEGIN
    v_start := clock_timestamp();

    INSERT INTO public.bi_refresh_log (
        modulo, database_name, data_inicio, views_count, status, execution_type
    ) VALUES (
        'INTERMED', current_database(), v_start, 3, 'EM_EXECUCAO', p_execution_type
    ) RETURNING id INTO v_log_id;

    BEGIN
        -- Actualizacao concorrente (zero bloqueio de leitura para o Power BI)
        REFRESH MATERIALIZED VIEW CONCURRENTLY mv_acolhimento;
        REFRESH MATERIALIZED VIEW CONCURRENTLY mv_utente;
        REFRESH MATERIALIZED VIEW CONCURRENTLY mv_entrevista;

        v_end := clock_timestamp();
        v_duration := ROUND(EXTRACT(EPOCH FROM (v_end - v_start))::NUMERIC, 2);

        UPDATE public.bi_refresh_log
        SET data_fim = v_end,
            duracao_segundos = v_duration,
            status = 'SUCESSO'
        WHERE id = v_log_id;

        RAISE NOTICE 'Materialized Views do INTERMED BI atualizadas com sucesso em % (Duracao: %s)', v_end, v_duration;

    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS
            v_err_msg = MESSAGE_TEXT,
            v_err_detail = PG_EXCEPTION_DETAIL;

        v_end := clock_timestamp();
        v_duration := ROUND(EXTRACT(EPOCH FROM (v_end - v_start))::NUMERIC, 2);

        UPDATE public.bi_refresh_log
        SET data_fim = v_end,
            duracao_segundos = v_duration,
            status = 'ERRO',
            mensagem_erro = COALESCE(v_err_msg, '') || CASE WHEN v_err_detail IS NOT NULL THEN ' | Detalhes: ' || v_err_detail ELSE '' END
        WHERE id = v_log_id;

        RAISE WARNING 'Falha ao atualizar Materialized Views do INTERMED BI: %', v_err_msg;
        RAISE EXCEPTION 'sp_refresh_emprego_bi falhou: %', v_err_msg;
    END;
END;
$$;

-- Agendamento horario, se o pg_cron estiver disponivel nesta base
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pg_cron') THEN
        PERFORM cron.unschedule(jobid)
        FROM cron.job
        WHERE jobname = 'job_refresh_emprego_bi';

        PERFORM cron.schedule('job_refresh_emprego_bi', '0 * * * *', 'CALL sp_refresh_emprego_bi(''CRON'');');
        RAISE NOTICE 'Agendamento do job_refresh_emprego_bi criado no pg_cron com sucesso!';
    ELSE
        RAISE NOTICE 'Extensao pg_cron nao instalada nesta base. Execute CALL sp_refresh_emprego_bi(); manualmente ou agende via SO.';
    END IF;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'pg_cron nao pode ser configurado nesta base: %. Execute CALL sp_refresh_emprego_bi(); manualmente ou via SO.', SQLERRM;
END $$;


-- ============================================================================
-- 6. MAPEAMENTO DAS VISTAS PARA OS INDICADORES DO DASHBOARD
--    (folha "Folhas Dashboard" da especificacao)
--
--  Visao Geral
--    Total de utentes registados .............. COUNT(vw_utente.id)
--    Total de acolhimento/atendimento ....... COUNT(vw_acolhimento.id)
--    Total de entrevistas agendadas ........... COUNT(vw_entrevista.id)
--    Total de utentes encaminhados ............ COUNT(vw_acolhimento.id)
--                                              WHERE data_encaminhamento IS NOT NULL
--    Total de sessoes de entrevistas realizadas COUNT(vw_entrevista.id)
--                                              WHERE data_realizacao IS NOT NULL
--    Evolucao anual de acolhimento ............ vw_acolhimento.ano
--
--  Acolhimento / Atendimento
--    Tempo medio de acolhimento ate ao encaminhamento
--                                              data_encaminhamento - data
--    Tempo medio de acolhimento ate ao agendamento da entrevista
--                                              data_agendamento_entrevista - data
--    Total de acolhimento por servico ......... tipo_servico / tipo_servico_desc
--    Total de acolhimento por canal .......... canal / canal_desc
--    Total de acolhimento por tecnico ........ tecnico (filtro por cefp)
--    Total de utentes registados por CEFP ..... vw_utente.cefp
--
--  Encaminhamento e Entrevistas
--    Total de sessoes de entrevistas realizadas vw_entrevista.data_realizacao
--    Total de utentes encaminhados por servico tipo_servico (data_encaminhamento)
--    Total de utentes encaminhados para balanco COUNT(...) WHERE sessao_balanco='sim'
--    Total de entrevistas por estado ......... estado_entrevista
--                                              (+ descricao do dominio
--                                                ESTADO_ENTREVISTA no IGRP)
--
--  Utentes
--    Total de utentes por sexo ................. vw_utente.sexo
--    Top 3 de servicos mais solicitados ...... vw_acolhimento.tipo_servico (gráfico Top N)
--    Total de utentes registados por CEFP ..... vw_utente.cefp
--    Evolucao anual de acolhimento ............ vw_acolhimento.ano
--                                              (filtros: ano, cefp, tipo_servico)
--
--  EXTRA (nao pedidos no documento, uteis para os KPI acima)
--    vw_acolhimento.data_colocacao                 -> "tempo ate colocacao"
--    vw_acolhimento.parecer_io / parecer_io_desc    -> resultado do parecer
-- ============================================================================


-- ============================================================================
-- 7. VALIDACAO (executar manualmente em db_emprego_bi)
-- ============================================================================
-- 7.1. A base de BI foi criada e as tabelas externas importadas? (12 esperadas)
-- SELECT foreign_table_name, foreign_server_name
-- FROM information_schema.foreign_tables
-- WHERE foreign_table_schema = 'public'
-- ORDER BY foreign_table_name;
--
-- 7.2. Contagens basicas
-- SELECT
--     (SELECT COUNT(*) FROM public.vw_acolhimento) AS acolhimentos,
--     (SELECT COUNT(*) FROM public.vw_utente)       AS utentes,
--     (SELECT COUNT(*) FROM public.vw_entrevista)   AS entrevistas,
--     (SELECT COUNT(*) FROM public.vw_acolhimento WHERE entrevista_agendada = 'sim')
--                                                AS acolhimentos_com_entrevista,
--     (SELECT COUNT(*) FROM public.vw_acolhimento WHERE data_colocacao IS NOT NULL)
--                                                AS acolhimentos_com_colocacao;
--
-- 7.3. O grao esta garantido? (todos devem devolver 0)
-- SELECT COUNT(*) - COUNT(DISTINCT id) FROM public.vw_acolhimento;
-- SELECT COUNT(*) - COUNT(DISTINCT id) FROM public.vw_utente;
-- SELECT COUNT(*) - COUNT(DISTINCT id) FROM public.vw_entrevista;
--
-- 7.4. Que valores existem hoje em tipo_servico? (ver AVISO 3 e AVISO 7)
--      Resultado actual: FORMACAO 13, EMPREGO 10, SUB_DESEMP 4, NULL 1
-- SELECT '['||tipo_servico||']' AS tipo_servico,
--        length(tipo_servico)    AS tamanho,
--        count(*)
-- FROM public.emprego_t_detalhes_acolhimento
-- GROUP BY tipo_servico
-- ORDER BY 3 DESC;
--
-- 7.5. Divergencia entre id_utente e pessoa_id (ver AVISO 4)
--      Resultado actual: 28 acolhimentos, 28 com id_utente, 0 sem utente.
-- SELECT
--     COUNT(*) FILTER (WHERE a.id_utente IS NULL)                     AS sem_id_utente,
--     COUNT(*) FILTER (WHERE a.id_utente IS NULL AND u.id IS NULL)     AS sem_utente,
--     COUNT(*) FILTER (WHERE a.id_utente IS NOT NULL
--                        AND a.id_utente <> u2.id)                    AS divergentes
-- FROM public.emprego_t_detalhes_acolhimento a
-- LEFT JOIN public.emprego_t_utente u  ON u.pessoa_id = a.id_pessoa
-- LEFT JOIN public.emprego_t_utente u2 ON u2.id = a.id_utente;
--
-- 7.6. O dominio do parecer carrega descricoes? (ver AVISO 6)
--      Resultado actual: com_parecer = 1, com_descricao = 0. A origem grava
--      parecer_io = '1' e o dominio so tem APROVAR / RECUSAR.
-- SELECT
--     (SELECT COUNT(*) FROM public.vw_acolhimento WHERE parecer_io IS NOT NULL) AS com_parecer,
--     (SELECT COUNT(*) FROM public.vw_acolhimento WHERE parecer_io IS NOT NULL
--        AND parecer_io_desc IS NOT NULL) AS com_descricao;
-- SELECT valor, description
-- FROM public.tbl_domain d
-- JOIN public.tbl_env env ON env.id = d.env_fk
-- WHERE UPPER(TRIM(d.dominio)) = 'PARECER_ENTREVISTA'
--   AND LOWER(TRIM(env.dad)) = 'interm_laboral';
--
-- 7.7. Cobertura dos dominios de analise
-- SELECT tipo_servico, tipo_servico_desc, COUNT(*) FROM public.vw_acolhimento
--  GROUP BY 1, 2 ORDER BY 3 DESC;   -- ver AVISO 7 (estao desalinhados)
-- SELECT canal,                COUNT(*) FROM public.vw_acolhimento GROUP BY 1 ORDER BY 2 DESC;
-- SELECT estado_entrevista,    COUNT(*) FROM public.vw_entrevista  GROUP BY 1 ORDER BY 2 DESC;
-- SELECT habilitacao_literaria, COUNT(*) FROM public.vw_acolhimento GROUP BY 1 ORDER BY 2 DESC;
-- SELECT tecnico,              COUNT(*) FROM public.vw_acolhimento GROUP BY 1 ORDER BY 2 DESC;
--
-- 7.8. Amostra da view principal
-- SELECT id, pessoa_id, id_utente, codigo_acolhimento, habilitacao_literaria,
--        data, tipo_servico, cefp, tecnico, data_encaminhamento,
--        entrevista_agendada, estado_entrevista, parecer_io, parecer_io_desc,
--        sessao_balanco, data_colocacao
-- FROM public.vw_acolhimento
-- ORDER BY data DESC
-- LIMIT 20;
--
-- 7.9. Primeiro refresh e historico
-- CALL sp_refresh_emprego_bi();
-- SELECT id, data_inicio, duracao_segundos, status, execution_type, mensagem_erro
-- FROM public.bi_refresh_log
-- ORDER BY data_inicio DESC
-- LIMIT 20;
--
-- 7.10. Job agendado
-- SELECT jobid, jobname, schedule, command
-- FROM cron.job
-- WHERE jobname = 'job_refresh_emprego_bi';


-- ============================================================================
-- 8. VALIDACAO AUTOMATICA POS-EXECUCAO
-- ============================================================================
DO $$
DECLARE
    v_foreign_tables INTEGER;
    v_mv_acolhimento BOOLEAN;
    v_mv_utente BOOLEAN;
    v_mv_entrevista BOOLEAN;
    v_idx BOOLEAN;
    c_nome TEXT;
BEGIN
    SELECT COUNT(*)
      INTO v_foreign_tables
      FROM information_schema.foreign_tables
     WHERE foreign_table_schema = 'public'
       AND foreign_table_name IN (
           'emprego_t_detalhes_acolhimento',
           'emprego_t_utente',
           'emprego_t_cefp',
           'emprego_t_agendamento_entrevista',
           'emprego_t_agendamento_balanco',
           'emprego_t_acolhimento_servico',
           'emprego_t_colocacao_candidato',
           'tbl_domain',
           'tbl_env',
           'pac_t_colaborador_contratado',
           'sgf_t_candidato',
           'sgf_t_candidato_selecionados'
       );

    IF v_foreign_tables <> 12 THEN
        RAISE EXCEPTION
            'Validacao falhou: esperadas 12 foreign tables, encontradas %. '
            'Verifique se o nome da base de origem esta correcto (AVISO 1).',
            v_foreign_tables;
    END IF;

    SELECT EXISTS (
        SELECT 1 FROM pg_matviews
        WHERE schemaname = 'public' AND matviewname = 'mv_acolhimento'
    ) INTO v_mv_acolhimento;

    SELECT EXISTS (
        SELECT 1 FROM pg_matviews
        WHERE schemaname = 'public' AND matviewname = 'mv_utente'
    ) INTO v_mv_utente;

    SELECT EXISTS (
        SELECT 1 FROM pg_matviews
        WHERE schemaname = 'public' AND matviewname = 'mv_entrevista'
    ) INTO v_mv_entrevista;

    IF NOT v_mv_acolhimento OR NOT v_mv_utente OR NOT v_mv_entrevista THEN
        RAISE EXCEPTION 'Validacao falhou: as 3 materialized views nao foram criadas corretamente.';
    END IF;

    -- Indice unico por view: obrigatorio ao REFRESH MATERIALIZED VIEW CONCURRENTLY
    FOR c_nome IN
        SELECT unnest(ARRAY['idx_mv_acolhimento_id', 'idx_mv_utente_id', 'idx_mv_entrevista_id'])
    LOOP
        SELECT EXISTS (
            SELECT 1 FROM pg_indexes
            WHERE schemaname = 'public' AND indexname = c_nome
        ) INTO v_idx;

        IF NOT v_idx THEN
            RAISE EXCEPTION 'Validacao falhou: indice % nao existe.', c_nome;
        END IF;
    END LOOP;

    IF to_regclass('public.bi_refresh_log') IS NULL THEN
        RAISE EXCEPTION 'Validacao falhou: tabela bi_refresh_log nao existe.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM pg_proc p
          JOIN pg_namespace n ON n.oid = p.pronamespace
         WHERE n.nspname = 'public'
           AND p.proname = 'sp_refresh_emprego_bi'
    ) THEN
        RAISE EXCEPTION 'Validacao falhou: procedure sp_refresh_emprego_bi nao existe.';
    END IF;

    RAISE NOTICE 'VALIDACAO OK: 12 foreign tables, 3 materialized views, indices e procedure encontrados.';
END
$$;

-- Resumo final
SELECT
    (SELECT COUNT(*) FROM public.vw_utente)                                   AS utentes,
    (SELECT COUNT(*) FROM public.vw_acolhimento)                              AS acolhimentos,
    (SELECT COUNT(*) FROM public.vw_entrevista)                              AS entrevistas,
    (SELECT COUNT(*) FROM public.vw_entrevista WHERE data_realizacao IS NOT NULL) AS entrevistas_realizadas,
    (SELECT COUNT(*) FROM public.vw_acolhimento WHERE sessao_balanco = 'sim') AS sessoes_balanco,
    (SELECT COUNT(*) FROM public.vw_acolhimento WHERE data_colocacao IS NOT NULL) AS colocacoes;
