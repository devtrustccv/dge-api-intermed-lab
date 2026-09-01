package cv.dge.dge_api_intermed_lab.application.perfilentidade.enums;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum EmpregoDominio {

    ESTADO_OFERTA_RASCUNHO("ESTADO_OFERTA", "RASCUNHO", "Rascunho"),
    ESTADO_OFERTA_EM_APROVACAO("ESTADO_OFERTA", "EM_APROVACAO", "Em aprova\u00e7\u00e3o"),
    ESTADO_OFERTA_ATIVA("ESTADO_OFERTA", "ATIVA", "Ativa"),
    ESTADO_OFERTA_FECHADA("ESTADO_OFERTA", "FECHADA", "Fechada"),
    ESTADO_OFERTA_CANCELADA("ESTADO_OFERTA", "CANCELADA", "Cancelada"),
    ESTADO_OFERTA_INATIVA("ESTADO_OFERTA", "INATIVA", "Inativa"),

    TIPO_OFERTA_ESTAGIO("TIPO_OFERTA", "OFERTA_ESTAGIO", "Oferta est\u00e1gio"),
    TIPO_OFERTA_EMPREGO("TIPO_OFERTA", "OFERTA_EMPREGO", "Oferta Emprego"),

    NIVEL_CONHECIMENTO_INEXISTENTE("NIVEL_CONHECIMENTO", "INEXISTENTE", "0 - Inexistente"),
    NIVEL_CONHECIMENTO_FRACO("NIVEL_CONHECIMENTO", "FRACO", "1 - Fraco"),
    NIVEL_CONHECIMENTO_SUFICIENTE("NIVEL_CONHECIMENTO", "SUFICIENTE", "2 - Suficiente"),
    NIVEL_CONHECIMENTO_BOM("NIVEL_CONHECIMENTO", "BOM", "3 - Bom"),
    NIVEL_CONHECIMENTO_MUITO_BOM("NIVEL_CONHECIMENTO", "MUITO_BOM", "4 - Muito Bom"),

    SIM_NAO_S("SIM_NAO", "S", "Sim"),
    SIM_NAO_N("SIM_NAO", "N", "N\u00e3o"),

    REGIME_CONTRATO_EFETIVO("REGIME_CONTRATO", "CONTRATO_EFETIVO", "Contrato Efetivo"),
    REGIME_CONTRATO_TERMO("REGIME_CONTRATO", "CONTRATO_TERMO", "Contrato a termo"),
    REGIME_CONTRATO_PRESTACAO_SERVICO("REGIME_CONTRATO", "PRESTACAO_SERVICO", "Presta\u00e7\u00e3o de Servi\u00e7os"),

    NIVEL_QUALIFICACAO_2("NIVEL_QUALIFICACAO", "2", "N\u00edvel 2"),
    NIVEL_QUALIFICACAO_3("NIVEL_QUALIFICACAO", "3", "N\u00edvel 3"),
    NIVEL_QUALIFICACAO_4("NIVEL_QUALIFICACAO", "4", "N\u00edvel 4"),
    NIVEL_QUALIFICACAO_5("NIVEL_QUALIFICACAO", "5", "N\u00edvel 5"),

    PARECER_ENTREVISTA_APROVAR("PARECER_ENTREVISTA", "APROVAR", "Aprovar"),
    PARECER_ENTREVISTA_RECUSAR("PARECER_ENTREVISTA", "RECUSAR", "Recusar"),

    HABILITACAO_LITERARIA_ENSINO_BASICO("HABILITACAO_LITERARIA", "ENSINO_BASICO", "Ensino B\u00e1sico"),
    HABILITACAO_LITERARIA_VIA_TEC("HABILITACAO_LITERARIA", "VIA_TEC", "12 \u00ba Via T\u00e9cnica"),
    HABILITACAO_LITERARIA_BACHAREL("HABILITACAO_LITERARIA", "BACHAREL", "Bacharelato"),
    HABILITACAO_LITERARIA_ENSINO_SECUNDARIO("HABILITACAO_LITERARIA", "ENSINO_SECUNDARIO", "Ensino Secund\u00e1rio"),
    HABILITACAO_LITERARIA_LICENCIATURA("HABILITACAO_LITERARIA", "LICENCIATURA", "Licenciatura"),
    HABILITACAO_LITERARIA_MESTRADO("HABILITACAO_LITERARIA", "MESTRADO", "Mestrado"),
    HABILITACAO_LITERARIA_DOUTORAMENTO("HABILITACAO_LITERARIA", "DOUTORAMENTO", "Doutoramento"),
    HABILITACAO_LITERARIA_POS_GRAD("HABILITACAO_LITERARIA", "POS_GRAD", "P\u00f3s-Gradua\u00e7\u00e3o"),

    ESTADO_A("ESTADO", "A", "Ativo"),
    ESTADO_I("ESTADO", "I", "Inativo"),

    STATUS_CANDIDATURA_TRIAGEM("STATUS_CANDIDATURA", "TRIAGEM", "Triagem"),
    STATUS_CANDIDATURA_APROVADO("STATUS_CANDIDATURA", "APROVADO", "Aprovado"),
    STATUS_CANDIDATURA_RECUSADO("STATUS_CANDIDATURA", "RECUSADO", "Recusado"),

    SITUACAO_PROFISSIONAL_NOVO_EMPREGO("SITUACAO_PROFISSIONAL", "1", "A procura de novo emprego"),
    SITUACAO_PROFISSIONAL_PRIMEIRO_EMPREGO("SITUACAO_PROFISSIONAL", "2", "A procura do primeiro emprego"),
    SITUACAO_PROFISSIONAL_PRIMEIRO_ESTAGIO("SITUACAO_PROFISSIONAL", "3", "A procura do primeiro estágio"),
    SITUACAO_PROFISSIONAL_DESEMPREGADO("SITUACAO_PROFISSIONAL", "4", "Desempregado"),
    SITUACAO_PROFISSIONAL_EMPREGADO("SITUACAO_PROFISSIONAL", "5", "Empregado"),
    SITUACAO_PROFISSIONAL_ESTAGIO("SITUACAO_PROFISSIONAL", "6", "Estágio"),

    CANDIDATURA_STATUS_PENDENTE("CANDIDATURA_STATUS", "PENDENTE", "Pendente"),
    CANDIDATURA_STATUS_PRE_SELECIONADO("CANDIDATURA_STATUS", "PRE_SELECIONADO", "Pré-selecionado"),
    CANDIDATURA_STATUS_SELECIONADO("CANDIDATURA_STATUS", "SELECIONADO", "Selecionado"),
    CANDIDATURA_STATUS_NAO_SELECIONADO("CANDIDATURA_STATUS", "NAO_SELECIONADO", "Não Selecionado"),

    CANAL_OFERTA_PORTAL("CANAL_OFERTA", "PORTAL", "Portal"),
    CANAL_OFERTA_BACKOFFICE("CANAL_OFERTA", "BACKOFFICE", "Backoffice"),

    TIPO_COLABORADOR_ORIENTADOR("TIPO_COLABORADOR", "ORIENTADOR", "Orientador"),
    TIPO_COLABORADOR_COORDENADOR("TIPO_COLABORADOR", "COORDENADOR", "Coordenador"),

    CANAL_ENTREVISTA_ONLINE("CANAL_ENTREVISTA", "ONLINE", "Online"),
    CANAL_ENTREVISTA_PRESENCIAL("CANAL_ENTREVISTA", "PRESENCIAL", "Presencial"),

    ESTADO_ENTREVISTA_PENDENTE("ESTADO_ENTREVISTA", "PENDENTE", "Pendente"),
    ESTADO_ENTREVISTA_REALIZADO("ESTADO_ENTREVISTA", "REALIZADO", "Realizado"),

    TIPO_COMPETENCIA_TECNICA("TIPO_COMPETENCIA", "COMP_TECNICA", "Compet\u00eancia T\u00e9cnica"),
    TIPO_COMPETENCIA_COMPORTAMENTAL("TIPO_COMPETENCIA", "COMP_COMPORTAMENTAL", "Compet\u00eancia Comportamental"),

    AVALIACAO_1("AVALIACAO", "1", "Insuficiente"),
    AVALIACAO_2("AVALIACAO", "2", "Regular"),
    AVALIACAO_3("AVALIACAO", "3", "Bom"),
    AVALIACAO_4("AVALIACAO", "4", "Muito Bom"),
    AVALIACAO_5("AVALIACAO", "5", "Excelente"),

    GRAU_SATISFACAO_1("GRAU_SATISFACAO", "1", "Pouco Satisfeito"),
    GRAU_SATISFACAO_2("GRAU_SATISFACAO", "2", "Satisfeito"),
    GRAU_SATISFACAO_3("GRAU_SATISFACAO", "3", "Muito Satisfeito"),

    TIPO_AVALIACAO_MENSAL("TIPO_AVALIACAO", "MENSAL", "Mensal"),
    TIPO_AVALIACAO_TRIMESTRAL("TIPO_AVALIACAO", "TRIMESTRAL", "Trimestral"),
    TIPO_AVALIACAO_SEMESTRAL("TIPO_AVALIACAO", "SEMESTRAL", "Semestral"),
    TIPO_AVALIACAO_FINAL("TIPO_AVALIACAO", "FINAL", "Final"),

    TIPO_ASSIDUIDADE_FALTA("TIPO_ASSIDUIDADE", "FALTA", "Falta"),
    TIPO_ASSIDUIDADE_PRESENTE("TIPO_ASSIDUIDADE", "PRESENTE", "Presente"),
    TIPO_ASSIDUIDADE_FALTA_JUSTIFICADA("TIPO_ASSIDUIDADE", "FALTA_JUSTIFICADA", "Falta justificada"),

    ESTADO_ASSIDUIDADE_APROVADO("ESTADO_ASSIDUIDADE", "APROVADO", "Aprovado"),
    ESTADO_ASSIDUIDADE_INDEFERIDO("ESTADO_ASSIDUIDADE", "INDEFERIDO", "Indeferido"),
    ESTADO_ASSIDUIDADE_PENDENTE("ESTADO_ASSIDUIDADE", "PENDENTE", "Pendente"),

    ESTADO_SERVICO_ATIVO("ESTADO_SERVICO", "A", "Ativo"),
    ESTADO_SERVICO_INATIVO("ESTADO_SERVICO", "I", "Inativo"),
    ESTADO_SERVICO_CANCELADO("ESTADO_SERVICO", "C", "Cancelado"),
    ESTADO_SERVICO_RASCUNHO("ESTADO_SERVICO", "R", "Rascunho"),
    ESTADO_SERVICO_ELIMINADO("ESTADO_SERVICO", "E", "Eliminado"),

    STATUS_ACEITACAO_CANDIDATO_ACEITE("STATUS_ACEITACAO_CANDIDATO", "ACEITE", "Aceite"),
    STATUS_ACEITACAO_CANDIDATO_RECUSADO("STATUS_ACEITACAO_CANDIDATO", "RECUSADO", "Recusado"),
    STATUS_ACEITACAO_CANDIDATO_PENDENTE("STATUS_ACEITACAO_CANDIDATO", "PENDENTE", "Pendente"),

    DECISAO_ASSIDUIDADE_APROVAR("DECISAO_ASSIDUIDADE", "APROVAR", "Aprovar"),
    DECISAO_ASSIDUIDADE_INDEFER("DECISAO_ASSIDUIDADE", "INDEFER", "Indeferir"),

    AGENDADO_POR_CEFP("AGENDADO_POR", "CEFP", "CEFP"),
    AGENDADO_POR_ENTIDADE_ACOLHEDORA("AGENDADO_POR", "ENTIDADE_ACOLHEDORA", "Entidade Acolhedora"),

    ESTADO_VISITA_TECNICA_PENDENTE("ESTADO_VISITA_TECNICA", "PENDENTE", "Pendente"),
    ESTADO_VISITA_TECNICA_AGENDADO("ESTADO_VISITA_TECNICA", "AGENDADO", "Agendado"),
    ESTADO_VISITA_TECNICA_INDEFERIDO("ESTADO_VISITA_TECNICA", "INDEFERIDO", "Indeferido"),
    ESTADO_VISITA_TECNICA_REALIZADO("ESTADO_VISITA_TECNICA", "REALIZADO", "Realizado"),

    PARECER_VISITA_DEFERIR("PARECER_VISITA", "DEFERIR", "Deferir"),
    PARECER_VISITA_INDEFERIR("PARECER_VISITA", "INDEFERIR", "Indeferir"),
    PARECER_VISITA_PROPOSTA_NOVA_DATA("PARECER_VISITA", "PROPOSTA_NOVA_DATA", "Proposta nova data"),

    CRITERIO_AVALIACAO_TECNICA("CRITERIO_AVALIACAO", "COMP_TECNICA", "Competencia tecnica"),
    CRITERIO_AVALIACAO_COMPORTAMENTAL("CRITERIO_AVALIACAO", "COMP_COMPORTAMENTAL", "Competencia comportamental"),
    CRITERIO_AVALIACAO_ASSIDUIDADE("CRITERIO_AVALIACAO", "ASSIDUIDADE", "Assiduidade"),
    CRITERIO_AVALIACAO_PONTUALIDADE("CRITERIO_AVALIACAO", "PONTUALIDADE", "Pontualidade");

    public static final String DOMINIO_ESTADO_OFERTA = "ESTADO_OFERTA";
    public static final String DOMINIO_TIPO_OFERTA = "TIPO_OFERTA";
    public static final String DOMINIO_NIVEL_CONHECIMENTO = "NIVEL_CONHECIMENTO";
    public static final String DOMINIO_SIM_NAO = "SIM_NAO";
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

    private final String dominio;
    private final String valor;
    private final String descricao;

    EmpregoDominio(String dominio, String valor, String descricao) {
        this.dominio = dominio;
        this.valor = valor;
        this.descricao = descricao;
    }

    public String getDominio() {
        return dominio;
    }

    public String getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Optional<EmpregoDominio> buscar(String dominio, String valor) {
        String dominioNormalizado = normalizar(dominio);
        String valorNormalizado = alias(dominioNormalizado, normalizar(valor));
        if (dominioNormalizado == null || valorNormalizado == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(item -> item.dominio.equals(dominioNormalizado))
                .filter(item -> item.valor.equals(valorNormalizado))
                .findFirst();
    }

    public static Optional<String> valorOficial(String dominio, String valor) {
        return buscar(dominio, valor).map(EmpregoDominio::getValor);
    }

    public static String descricao(String dominio, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return valor;
        }
        return buscar(dominio, valor)
                .map(EmpregoDominio::getDescricao)
                .orElse(valor);
    }

    public static List<EmpregoDominio> listarPorDominio(String dominio) {
        String dominioNormalizado = normalizar(dominio);
        return Arrays.stream(values())
                .filter(item -> item.dominio.equals(dominioNormalizado))
                .toList();
    }

    public static List<String> listarDominios() {
        return Arrays.stream(values())
                .map(EmpregoDominio::getDominio)
                .distinct()
                .sorted()
                .toList();
    }

    private static String alias(String dominio, String valor) {
        if (dominio == null || valor == null) {
            return valor;
        }
        return switch (dominio) {
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
