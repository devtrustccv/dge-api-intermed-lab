package cv.dge.dge_api_intermed_lab.application.orientacao.mapper;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoEntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.RequisitoResponse;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.Requisito;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OrientacaoMapper {

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
                servico.getDetalhesServico(),
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
        dados.put("detalhes", acolhimento.getDetalhes());
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
}
