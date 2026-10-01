package cv.dge.dge_api_intermed_lab.application.orientacao.mapper;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoEntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.RequisitoResponse;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.Requisito;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OrientacaoMapper {

    private final DocumentService documentService;

    public OrientacaoMapper(DocumentService documentService) {
        this.documentService = documentService;
    }

    public OrientacaoServicoResponse toServicoResponse(AcolhimentoServico servico) {
        if (servico == null) {
            return null;
        }
        return new OrientacaoServicoResponse(
                servico.getId(),
                servico.getIdEntrevista(),
                servico.getIdAcolhimento(),
                servico.getIdUtente(),
                servico.getTipoUtente(),
                servico.getTipoUtenteDesc(),
                servico.getTipoServico(),
                servico.getTipoServicoDesc(),
                servico.getNecessidadeAnalise(),
                normalizarDetalhesDocumento(servico.getDetalhesServico()),
                servico.getDetalhesAnalise()
        );
    }

    public OrientacaoEntrevistaResponse toEntrevistaResponse(
            AgendamentoEntrevista entrevista,
            DetalhesAcolhimento acolhimento,
            AcolhimentoServico servico
    ) {
        if (entrevista == null) {
            return null;
        }
        return new OrientacaoEntrevistaResponse(
                entrevista.getId(),
                entrevista.getIdAcolhimento(),
                entrevista.getIdUtente(),
                entrevista.getIdTecnico(),
                entrevista.getNomeTecnico(),
                entrevista.getDataEntrevista(),
                entrevista.getHoraInicio(),
                entrevista.getHoraFim(),
                entrevista.getLocal(),
                entrevista.getStatusEntrevista(),
                entrevista.getIdCefp(),
                entrevista.getCefp(),
                entrevista.getTipoServico(),
                entrevista.getCanal(),
                entrevista.getLocalEntrevista(),
                entrevista.getResultadoEntrevista(),
                entrevista.getParecerIo(),
                entrevista.getObsParecerIo(),
                entrevista.getPathResultado(),
                entrevista.getDateCreate(),
                entrevista.getUserCreate(),
                entrevista.getDateUpdate(),
                entrevista.getUserUpdate(),
                toAcolhimentoMap(acolhimento),
                toServicoResponse(servico)
        );
    }

    public RequisitoResponse toRequisitoResponse(Requisito requisito) {
        return new RequisitoResponse(
                requisito.getId(),
                requisito.getRequisito(),
                requisito.getTipoServico(),
                requisito.getDateCreate(),
                requisito.getUserCreate(),
                requisito.getEstado(),
                requisito.getDateUpdate(),
                requisito.getUserUpdate()
        );
    }

    private Map<String, Object> toAcolhimentoMap(DetalhesAcolhimento acolhimento) {
        if (acolhimento == null) {
            return null;
        }
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("id", acolhimento.getId());
        dados.put("idPessoa", acolhimento.getIdPessoa());
        dados.put("idUtente", acolhimento.getIdUtente());
        dados.put("idEntidade", acolhimento.getIdEntidade());
        dados.put("denominacaoUtente", acolhimento.getDenominacaoUtente());
        dados.put("nif", acolhimento.getNif());
        dados.put("cefpId", acolhimento.getCefpId());
        dados.put("orgId", acolhimento.getOrgId());
        dados.put("tipoUtente", acolhimento.getTipoUtente());
        dados.put("tipoUtenteDesc", acolhimento.getTipoUtenteDesc());
        dados.put("tipoServico", acolhimento.getTipoServico());
        dados.put("tipoServicoDesc", acolhimento.getTipoServicoDesc());
        dados.put("canal", acolhimento.getCanal());
        dados.put("canalDesc", acolhimento.getCanalDesc());
        dados.put("detalhes", normalizarDetalhesDocumento(acolhimento.getDetalhes()));
        dados.put("idTecnicoAtendimento", acolhimento.getIdTecnicoAtendimento());
        dados.put("tecnicoAtendimento", acolhimento.getTecnicoAtendimento());
        dados.put("fonteInformacao", acolhimento.getFonteInformacao());
        dados.put("statusEntrevista", acolhimento.getStatusEntrevista());
        dados.put("numInscricao", acolhimento.getNumInscricao());
        dados.put("dateCreate", acolhimento.getDateCreate());
        dados.put("userCreate", acolhimento.getUserCreate());
        dados.put("dateUpdate", acolhimento.getDateUpdate());
        dados.put("userUpdate", acolhimento.getUserUpdate());
        return dados;
    }

    private Map<String, Object> normalizarDetalhesDocumento(Map<String, Object> detalhes) {
        if (detalhes == null) {
            return null;
        }

        Map<String, Object> normalizados = new LinkedHashMap<>(detalhes);
        Object valorAnexos = detalhes.get("anexos");
        if (!(valorAnexos instanceof List<?> anexos)) {
            return normalizados;
        }

        List<Object> anexosNormalizados = new ArrayList<>();
        for (Object item : anexos) {
            if (!(item instanceof Map<?, ?> mapa)) {
                anexosNormalizados.add(item);
                continue;
            }

            Map<String, Object> anexo = new LinkedHashMap<>();
            mapa.forEach((chave, valor) -> {
                if (chave != null) {
                    anexo.put(chave.toString(), valor);
                }
            });
            normalizarLinkDocumento(anexo);
            anexosNormalizados.add(anexo);
        }
        normalizados.put("anexos", anexosNormalizados);
        return normalizados;
    }

    private void normalizarLinkDocumento(Map<String, Object> anexo) {
        String path = primeiroTexto(anexo, "anexo", "path", "caminho");
        String url = primeiroTexto(anexo, "ver_documento", "url", "previewUrl");
        String origem = path != null ? path : url;
        if (origem == null) {
            return;
        }

        String linkAtual = documentService.gerarLinkPublico(origem);
        anexo.put("ver_documento", linkAtual);
        if (anexo.containsKey("ver_documento_desc")) {
            anexo.put("ver_documento_desc", linkAtual);
        }
        if (anexo.containsKey("url")) {
            anexo.put("url", linkAtual);
        }
        if (anexo.containsKey("previewUrl")) {
            anexo.put("previewUrl", linkAtual);
        }
    }

    private String primeiroTexto(Map<String, Object> dados, String... chaves) {
        for (String chave : chaves) {
            Object valor = dados.get(chave);
            if (valor != null && !valor.toString().isBlank()) {
                return valor.toString().trim();
            }
        }
        return null;
    }
}
