-- Campos de texto livre recebidos ou propagados pelas APIs de emprego.
-- Os limites dos campos de domínio, códigos e contactos permanecem inalterados.
ALTER TABLE IF EXISTS emprego_t_candidatura_oferta
    ALTER COLUMN nome TYPE VARCHAR,
    ALTER COLUMN habilitacao_academica TYPE VARCHAR,
    ALTER COLUMN motivo_recusa TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_entrevista_oferta
    ALTER COLUMN nome TYPE VARCHAR,
    ALTER COLUMN local_entrevista TYPE VARCHAR,
    ALTER COLUMN resultado_entrevista TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_colocacao_candidato
    ALTER COLUMN denominacao_entidade TYPE VARCHAR,
    ALTER COLUMN nome TYPE VARCHAR,
    ALTER COLUMN descricao TYPE VARCHAR,
    ALTER COLUMN cefp TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_avaliacao_estagiario
    ALTER COLUMN nome TYPE VARCHAR,
    ALTER COLUMN periodo_referencia TYPE VARCHAR,
    ALTER COLUMN interesse_contratacao TYPE VARCHAR,
    ALTER COLUMN observacao TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_visitas
    ALTER COLUMN visitante TYPE VARCHAR,
    ALTER COLUMN objetivos TYPE VARCHAR,
    ALTER COLUMN conteudo_reuniao TYPE VARCHAR,
    ALTER COLUMN supervisor_participante TYPE VARCHAR,
    ALTER COLUMN motivo_indeferimento TYPE VARCHAR,
    ALTER COLUMN cefp TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_assiduidade
    ALTER COLUMN denominacao_entidade TYPE VARCHAR,
    ALTER COLUMN nome TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_relatorio_acomp
    ALTER COLUMN denominacao_entidade TYPE VARCHAR,
    ALTER COLUMN nome TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_entidade_colaborador
    ALTER COLUMN nome TYPE VARCHAR,
    ALTER COLUMN email TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_intermediacao_candidato
    ALTER COLUMN nome TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_certificado_estagio
    ALTER COLUMN nome TYPE VARCHAR,
    ALTER COLUMN naturalidade TYPE VARCHAR,
    ALTER COLUMN habilitacao_academica TYPE VARCHAR,
    ALTER COLUMN nome_entidade TYPE VARCHAR;

-- Referências externas e documentais podem crescer com o host e os parâmetros.
ALTER TABLE IF EXISTS emprego_t_cefp
    ALTER COLUMN url_site TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_agendamento_entrevista
    ALTER COLUMN path_resultado TYPE VARCHAR;

ALTER TABLE IF EXISTS emprego_t_parametrizacao_report
    ALTER COLUMN logotipo_iefp TYPE VARCHAR,
    ALTER COLUMN logotipo_dge TYPE VARCHAR;
