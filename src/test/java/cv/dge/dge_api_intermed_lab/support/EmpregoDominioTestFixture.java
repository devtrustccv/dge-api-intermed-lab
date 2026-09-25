package cv.dge.dge_api_intermed_lab.support;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoDominioResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.EmpregoDominioService;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.IgrpDominioRepository;
import java.util.List;

/** Catálogo IGRP em memória usado exclusivamente pelos testes unitários. */
public final class EmpregoDominioTestFixture {

    private static final List<EmpregoDominioResponse> ITENS = List.of(
            item("ESTADO_OFERTA", "RASCUNHO", "Rascunho"),
            item("ESTADO_OFERTA", "EM_APROVACAO", "Em aprovação"),
            item("ESTADO_OFERTA", "ATIVA", "Ativa"),
            item("ESTADO_OFERTA", "FECHADA", "Fechada"),
            item("ESTADO_OFERTA", "CANCELADA", "Cancelada"),
            item("ESTADO_OFERTA", "INATIVA", "Inativa"),
            item("TIPO_OFERTA", "OFERTA_ESTAGIO", "Oferta estágio"),
            item("TIPO_OFERTA", "OFERTA_EMPREGO", "Oferta Emprego"),
            item("NIVEL_CONHECIMENTO", "INEXISTENTE", "0 - Inexistente"),
            item("NIVEL_CONHECIMENTO", "FRACO", "1 - Fraco"),
            item("NIVEL_CONHECIMENTO", "SUFICIENTE", "2 - Suficiente"),
            item("NIVEL_CONHECIMENTO", "BOM", "3 - Bom"),
            item("NIVEL_CONHECIMENTO", "MUITO_BOM", "4 - Muito Bom"),
            item("SIM_NAO", "S", "Sim"),
            item("SIM_NAO", "N", "Não"),
            item("SEXO", "M", "Masculino"),
            item("SEXO", "F", "Feminino"),
            item("REGIME_CONTRATO", "CONTRATO_EFETIVO", "Contrato Efetivo"),
            item("REGIME_CONTRATO", "CONTRATO_TERMO", "Contrato a termo"),
            item("REGIME_CONTRATO", "PRESTACAO_SERVICO", "Prestação de Serviços"),
            item("NIVEL_QUALIFICACAO", "2", "Nível 2"),
            item("NIVEL_QUALIFICACAO", "3", "Nível 3"),
            item("NIVEL_QUALIFICACAO", "4", "Nível 4"),
            item("NIVEL_QUALIFICACAO", "5", "Nível 5"),
            item("PARECER_ENTREVISTA", "APROVAR", "Aprovar"),
            item("PARECER_ENTREVISTA", "RECUSAR", "Recusar"),
            item("HABILITACAO_LITERARIA", "ENSINO_BASICO", "Ensino Básico"),
            item("HABILITACAO_LITERARIA", "VIA_TEC", "12 º Via Técnica"),
            item("HABILITACAO_LITERARIA", "BACHAREL", "Bacharelato"),
            item("HABILITACAO_LITERARIA", "ENSINO_SECUNDARIO", "Ensino Secundário"),
            item("HABILITACAO_LITERARIA", "LICENCIATURA", "Licenciatura"),
            item("HABILITACAO_LITERARIA", "MESTRADO", "Mestrado"),
            item("HABILITACAO_LITERARIA", "DOUTORAMENTO", "Doutoramento"),
            item("HABILITACAO_LITERARIA", "POS_GRAD", "Pós-Graduação"),
            item("HABILITACAO_LITERARIA", "FORMACAO_PROFISSIONAL", "Formação Profissional"),
            item("ESTADO", "A", "Ativo"),
            item("ESTADO", "I", "Inativo"),
            item("STATUS_CANDIDATURA", "TRIAGEM", "Triagem"),
            item("STATUS_CANDIDATURA", "APROVADO", "Aprovado"),
            item("STATUS_CANDIDATURA", "RECUSADO", "Recusado"),
            item("SITUACAO_PROFISSIONAL", "1", "A procura de novo emprego"),
            item("SITUACAO_PROFISSIONAL", "2", "A procura do primeiro emprego"),
            item("SITUACAO_PROFISSIONAL", "3", "A procura do primeiro estágio"),
            item("SITUACAO_PROFISSIONAL", "4", "Desempregado"),
            item("SITUACAO_PROFISSIONAL", "5", "Empregado"),
            item("SITUACAO_PROFISSIONAL", "6", "Estágio"),
            item("CANDIDATURA_STATUS", "PENDENTE", "Pendente"),
            item("CANDIDATURA_STATUS", "PRE_SELECIONADO", "Pré-selecionado"),
            item("CANDIDATURA_STATUS", "SELECIONADO", "Selecionado"),
            item("CANDIDATURA_STATUS", "NAO_SELECIONADO", "Não Selecionado"),
            item("CANAL_OFERTA", "PORTAL", "Portal"),
            item("CANAL_OFERTA", "BACKOFFICE", "Backoffice"),
            item("TIPO_COLABORADOR", "ORIENTADOR", "Orientador"),
            item("TIPO_COLABORADOR", "COORDENADOR", "Coordenador"),
            item("CANAL_ENTREVISTA", "ONLINE", "Online"),
            item("CANAL_ENTREVISTA", "PRESENCIAL", "Presencial"),
            item("ESTADO_ENTREVISTA", "PENDENTE", "Pendente"),
            item("ESTADO_ENTREVISTA", "REALIZADO", "Realizado"),
            item("TIPO_COMPETENCIA", "COMP_TECNICA", "Competência Técnica"),
            item("TIPO_COMPETENCIA", "COMP_COMPORTAMENTAL", "Competência Comportamental"),
            item("AVALIACAO", "1", "Insuficiente"),
            item("AVALIACAO", "2", "Regular"),
            item("AVALIACAO", "3", "Bom"),
            item("AVALIACAO", "4", "Muito Bom"),
            item("AVALIACAO", "5", "Excelente"),
            item("GRAU_SATISFACAO", "1", "Pouco Satisfeito"),
            item("GRAU_SATISFACAO", "2", "Satisfeito"),
            item("GRAU_SATISFACAO", "3", "Muito Satisfeito"),
            item("TIPO_AVALIACAO", "MENSAL", "Mensal"),
            item("TIPO_AVALIACAO", "TRIMESTRAL", "Trimestral"),
            item("TIPO_AVALIACAO", "SEMESTRAL", "Semestral"),
            item("TIPO_AVALIACAO", "FINAL", "Final"),
            item("TIPO_ASSIDUIDADE", "FALTA", "Falta"),
            item("TIPO_ASSIDUIDADE", "PRESENTE", "Presente"),
            item("TIPO_ASSIDUIDADE", "FALTA_JUSTIFICADA", "Falta justificada"),
            item("ESTADO_ASSIDUIDADE", "APROVADO", "Aprovado"),
            item("ESTADO_ASSIDUIDADE", "INDEFERIDO", "Indeferido"),
            item("ESTADO_ASSIDUIDADE", "PENDENTE", "Pendente"),
            item("ESTADO_SERVICO", "A", "Ativo"),
            item("ESTADO_SERVICO", "I", "Inativo"),
            item("ESTADO_SERVICO", "C", "Cancelado"),
            item("ESTADO_SERVICO", "R", "Rascunho"),
            item("ESTADO_SERVICO", "E", "Eliminado"),
            item("STATUS_ACEITACAO_CANDIDATO", "ACEITE", "Aceite"),
            item("STATUS_ACEITACAO_CANDIDATO", "RECUSADO", "Recusado"),
            item("STATUS_ACEITACAO_CANDIDATO", "PENDENTE", "Pendente"),
            item("DECISAO_ASSIDUIDADE", "APROVAR", "Aprovar"),
            item("DECISAO_ASSIDUIDADE", "INDEFER", "Indeferir"),
            item("AGENDADO_POR", "CEFP", "CEFP"),
            item("AGENDADO_POR", "ENTIDADE_ACOLHEDORA", "Entidade Acolhedora"),
            item("ESTADO_VISITA_TECNICA", "PENDENTE", "Pendente"),
            item("ESTADO_VISITA_TECNICA", "AGENDADO", "Agendado"),
            item("ESTADO_VISITA_TECNICA", "INDEFERIDO", "Indeferido"),
            item("ESTADO_VISITA_TECNICA", "REALIZADO", "Realizado"),
            item("PARECER_VISITA", "DEFERIR", "Deferir"),
            item("PARECER_VISITA", "INDEFERIR", "Indeferir"),
            item("PARECER_VISITA", "PROPOSTA_NOVA_DATA", "Proposta nova data"),
            item("CRITERIO_AVALIACAO", "COMP_TECNICA", "Competência técnica"),
            item("CRITERIO_AVALIACAO", "COMP_COMPORTAMENTAL", "Competência comportamental"),
            item("CRITERIO_AVALIACAO", "ASSIDUIDADE", "Assiduidade"),
            item("CRITERIO_AVALIACAO", "PONTUALIDADE", "Pontualidade")
    );

    private EmpregoDominioTestFixture() {
    }

    public static EmpregoDominioService criar() {
        IgrpDominioRepository repository = mock(IgrpDominioRepository.class);
        lenient().when(repository.listarPorDominio(anyString(), nullable(String.class)))
                .thenAnswer(invocacao -> {
                    String dominio = invocacao.getArgument(0);
                    return ITENS.stream()
                            .filter(item -> item.dominio().equalsIgnoreCase(dominio))
                            .toList();
                });
        return new EmpregoDominioService(repository);
    }

    private static EmpregoDominioResponse item(String dominio, String valor, String descricao) {
        return new EmpregoDominioResponse(
                null,
                descricao,
                "PRIVATE",
                dominio,
                null,
                "ATIVE",
                valor,
                13
        );
    }
}
