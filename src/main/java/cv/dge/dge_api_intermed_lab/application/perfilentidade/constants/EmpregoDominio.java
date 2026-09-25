package cv.dge.dge_api_intermed_lab.application.perfilentidade.constants;

import java.text.Normalizer;

/**
 * Identificadores dos domínios usados pela aplicação.
 *
 * <p>Os valores e descrições não ficam definidos nesta classe. A fonte de
 * verdade é a tabela {@code tbl_domain} da base {@code db_igrp_dge}.</p>
 */
public final class EmpregoDominio {

    public static final String DOMINIO_ESTADO_OFERTA = "ESTADO_OFERTA";
    public static final String DOMINIO_TIPO_OFERTA = "TIPO_OFERTA";
    public static final String DOMINIO_NIVEL_CONHECIMENTO = "NIVEL_CONHECIMENTO";
    public static final String DOMINIO_SIM_NAO = "SIM_NAO";
    public static final String DOMINIO_SEXO = "SEXO";
    public static final String DOMINIO_REGIME_CONTRATO = "REGIME_CONTRATO";
    public static final String DOMINIO_NIVEL_QUALIFICACAO = "NIVEL_QUALIFICACAO";
    public static final String DOMINIO_PARECER_ENTREVISTA = "PARECER_ENTREVISTA";
    public static final String DOMINIO_HABILITACAO_LITERARIA = "HABILITACAO_LITERARIA";
    public static final String DOMINIO_ESTADO = "ESTADO";
    public static final String DOMINIO_STATUS_CANDIDATURA = "STATUS_CANDIDATURA";
    public static final String DOMINIO_SITUACAO_PROFISSIONAL = "SITUACAO_PROFISSIONAL";
    public static final String DOMINIO_CANDIDATURA_STATUS = "CANDIDATURA_STATUS";
    public static final String DOMINIO_CANAL_OFERTA = "CANAL_OFERTA";
    public static final String DOMINIO_TIPO_COLABORADOR = "TIPO_COLABORADOR";
    public static final String DOMINIO_CANAL_ENTREVISTA = "CANAL_ENTREVISTA";
    public static final String DOMINIO_ESTADO_ENTREVISTA = "ESTADO_ENTREVISTA";
    public static final String DOMINIO_TIPO_COMPETENCIA = "TIPO_COMPETENCIA";
    public static final String DOMINIO_AVALIACAO = "AVALIACAO";
    public static final String DOMINIO_GRAU_SATISFACAO = "GRAU_SATISFACAO";
    public static final String DOMINIO_TIPO_AVALIACAO = "TIPO_AVALIACAO";
    public static final String DOMINIO_TIPO_ASSIDUIDADE = "TIPO_ASSIDUIDADE";
    public static final String DOMINIO_ESTADO_ASSIDUIDADE = "ESTADO_ASSIDUIDADE";
    public static final String DOMINIO_ESTADO_SERVICO = "ESTADO_SERVICO";
    public static final String DOMINIO_STATUS_ACEITACAO_CANDIDATO = "STATUS_ACEITACAO_CANDIDATO";
    public static final String DOMINIO_DECISAO_ASSIDUIDADE = "DECISAO_ASSIDUIDADE";
    public static final String DOMINIO_AGENDADO_POR = "AGENDADO_POR";
    public static final String DOMINIO_AGENDAMENTO_POR = DOMINIO_AGENDADO_POR;
    public static final String DOMINIO_ESTADO_VISITA_TECNICA = "ESTADO_VISITA_TECNICA";
    public static final String DOMINIO_PARECER_VISITA = "PARECER_VISITA";
    public static final String DOMINIO_CRITERIO_AVALIACAO = "CRITERIO_AVALIACAO";

    private EmpregoDominio() {
    }

    public static String nomeCampo(String dominio) {
        String dominioNormalizado = normalizar(dominio);
        if (dominioNormalizado == null) {
            return "Opção";
        }
        return switch (dominioNormalizado) {
            case DOMINIO_ESTADO_OFERTA -> "Estado da oferta";
            case DOMINIO_TIPO_OFERTA -> "Tipo de oferta";
            case DOMINIO_NIVEL_CONHECIMENTO -> "Nível de conhecimento";
            case DOMINIO_SIM_NAO -> "Opção Sim/Não";
            case DOMINIO_SEXO -> "Sexo";
            case DOMINIO_REGIME_CONTRATO -> "Regime de contrato";
            case DOMINIO_NIVEL_QUALIFICACAO -> "Nível de qualificação";
            case DOMINIO_PARECER_ENTREVISTA -> "Parecer da entrevista";
            case DOMINIO_HABILITACAO_LITERARIA -> "Habilitação literária";
            case DOMINIO_ESTADO -> "Estado";
            case DOMINIO_STATUS_CANDIDATURA -> "Estado da candidatura";
            case DOMINIO_SITUACAO_PROFISSIONAL -> "Situação profissional";
            case DOMINIO_CANDIDATURA_STATUS -> "Estado da candidatura ao serviço";
            case DOMINIO_CANAL_OFERTA -> "Canal da oferta";
            case DOMINIO_TIPO_COLABORADOR -> "Tipo de colaborador";
            case DOMINIO_CANAL_ENTREVISTA -> "Modalidade da entrevista";
            case DOMINIO_ESTADO_ENTREVISTA -> "Estado da entrevista";
            case DOMINIO_TIPO_COMPETENCIA -> "Tipo de competência";
            case DOMINIO_AVALIACAO -> "Classificação da avaliação";
            case DOMINIO_GRAU_SATISFACAO -> "Grau de satisfação";
            case DOMINIO_TIPO_AVALIACAO -> "Tipo de avaliação";
            case DOMINIO_TIPO_ASSIDUIDADE -> "Tipo de assiduidade";
            case DOMINIO_ESTADO_ASSIDUIDADE -> "Estado da assiduidade";
            case DOMINIO_ESTADO_SERVICO -> "Estado do serviço";
            case DOMINIO_STATUS_ACEITACAO_CANDIDATO -> "Estado de aceitação do candidato";
            case DOMINIO_DECISAO_ASSIDUIDADE -> "Decisão da assiduidade";
            case DOMINIO_AGENDADO_POR -> "Responsável pelo agendamento";
            case DOMINIO_ESTADO_VISITA_TECNICA -> "Estado da visita técnica";
            case DOMINIO_PARECER_VISITA -> "Parecer da visita técnica";
            case DOMINIO_CRITERIO_AVALIACAO -> "Critério da avaliação";
            default -> dominioNormalizado.replace('_', ' ');
        };
    }

    public static String mensagemCampoObrigatorio(String dominio) {
        return "O campo \"" + nomeCampo(dominio) + "\" é obrigatório.";
    }

    public static String alias(String dominio, String valor) {
        String dominioNormalizado = normalizar(dominio);
        if (dominioNormalizado == null || valor == null) {
            return valor;
        }
        return switch (dominioNormalizado) {
            case DOMINIO_TIPO_OFERTA -> switch (valor) {
                case "EMPREGO" -> "OFERTA_EMPREGO";
                case "ESTAGIO", "ESTAGIO_PROFISSIONAL" -> "OFERTA_ESTAGIO";
                default -> valor;
            };
            case DOMINIO_NIVEL_QUALIFICACAO -> switch (valor) {
                case "NIVEL_2" -> "2";
                case "NIVEL_3" -> "3";
                case "NIVEL_4" -> "4";
                case "NIVEL_5" -> "5";
                default -> valor;
            };
            case DOMINIO_SEXO -> switch (valor) {
                case "MASCULINO", "MALE" -> "M";
                case "FEMININO", "FEMALE" -> "F";
                default -> valor;
            };
            case DOMINIO_HABILITACAO_LITERARIA -> switch (valor) {
                case "12_ANO", "12", "SECUNDARIO" -> "ENSINO_SECUNDARIO";
                case "BASICO" -> "ENSINO_BASICO";
                case "POS_GRADUACAO", "POS_GRADUACAO_" -> "POS_GRAD";
                default -> valor;
            };
            case DOMINIO_REGIME_CONTRATO -> switch (valor) {
                case "TERMO" -> "CONTRATO_TERMO";
                case "EFETIVO", "CONTRATO_EFECTIVO" -> "CONTRATO_EFETIVO";
                default -> valor;
            };
            case DOMINIO_ESTADO -> switch (valor) {
                case "ATIVO" -> "A";
                case "INATIVO", "INACTIVO" -> "I";
                default -> valor;
            };
            case DOMINIO_STATUS_CANDIDATURA -> switch (valor) {
                case "APROVAR", "APROVADA" -> "APROVADO";
                case "RECUSAR", "RECUSADA" -> "RECUSADO";
                default -> valor;
            };
            case DOMINIO_CANDIDATURA_STATUS -> switch (valor) {
                case "TRIAGEM" -> "PENDENTE";
                case "PRESELECIONADO", "PRE-SELECIONADO", "PRE SELECIONADO" -> "PRE_SELECIONADO";
                case "APROVADO", "APROVADA" -> "SELECIONADO";
                case "RECUSADO", "RECUSADA", "NAO-SELECIONADO", "NAO SELECIONADO" -> "NAO_SELECIONADO";
                default -> valor;
            };
            case DOMINIO_ESTADO_SERVICO -> switch (valor) {
                case "ATIVO", "ATIVA" -> "A";
                case "INATIVO", "INATIVA", "INACTIVO", "INACTIVA" -> "I";
                case "RASCUNHO", "EM_RASCUNHO" -> "R";
                case "CANCELADO", "CANCELADA" -> "C";
                case "ELIMINADO", "ELIMINADA", "REMOVIDO", "REMOVIDA" -> "E";
                default -> valor;
            };
            case DOMINIO_STATUS_ACEITACAO_CANDIDATO -> switch (valor) {
                case "ACEITO", "ACEITA", "ACEITAR" -> "ACEITE";
                case "RECUSAR", "RECUSADA" -> "RECUSADO";
                default -> valor;
            };
            case DOMINIO_CANAL_OFERTA -> switch (valor) {
                case "ONLINE" -> "PORTAL";
                case "PRESENCIAL" -> "BACKOFFICE";
                default -> valor;
            };
            case DOMINIO_PARECER_ENTREVISTA -> switch (valor) {
                case "APROVADO", "APROVADA", "FAVORAVEL" -> "APROVAR";
                case "RECUSADO", "RECUSADA", "DESFAVORAVEL" -> "RECUSAR";
                default -> valor;
            };
            case DOMINIO_DECISAO_ASSIDUIDADE -> switch (valor) {
                case "APROVADO", "APROVADA" -> "APROVAR";
                case "INDEFERIR", "INDEFERIDO", "INDEFERIDA" -> "INDEFER";
                default -> valor;
            };
            case DOMINIO_AGENDADO_POR -> switch (valor) {
                case "ENTIDADE", "ENTIDADE_ACOLHIMENTO" -> "ENTIDADE_ACOLHEDORA";
                default -> valor;
            };
            case DOMINIO_PARECER_VISITA -> switch (valor) {
                case "DEFERIDO", "APROVAR", "APROVADO" -> "DEFERIR";
                case "INDEFERIDO", "RECUSAR", "RECUSADO" -> "INDEFERIR";
                case "NOVA_DATA", "PROPOR_NOVA_DATA" -> "PROPOSTA_NOVA_DATA";
                default -> valor;
            };
            default -> valor;
        };
    }

    public static String normalizar(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        String semAcentos = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos
                .replace("\u200b", "")
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");
    }
}
