package cv.dge.dge_api_intermed_lab.application.orientacao.service;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoEntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.AcolhimentoServico;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.AgendamentoEntrevista;
import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesAcolhimento;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.AcolhimentoServicoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.AgendamentoEntrevistaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository.DetalhesAcolhimentoRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrientacaoEntrevistaService {

    private final AgendamentoEntrevistaRepository agendamentoEntrevistaRepository;
    private final DetalhesAcolhimentoRepository detalhesAcolhimentoRepository;
    private final AcolhimentoServicoRepository acolhimentoServicoRepository;

    @Transactional(readOnly = true)
    public OrientacaoEntrevistaResponse buscarPorId(Integer id) {
        AgendamentoEntrevista entrevista = agendamentoEntrevistaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrevista nao encontrada."));

        DetalhesAcolhimento acolhimento = entrevista.getIdAcolhimento() == null
                ? null
                : detalhesAcolhimentoRepository.findById(entrevista.getIdAcolhimento()).orElse(null);

        AcolhimentoServico servico = buscarServicoDaEntrevista(entrevista);

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
                mapearAcolhimento(acolhimento),
                mapearServico(servico)
        );
    }

    private Map<String, Object> mapearAcolhimento(DetalhesAcolhimento acolhimento) {
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

    private AcolhimentoServico buscarServicoDaEntrevista(AgendamentoEntrevista entrevista) {
        String tipoServico = entrevista.getTipoServico();
        if (tipoServico != null && !tipoServico.isBlank()) {
            return acolhimentoServicoRepository
                    .findFirstByIdEntrevistaAndTipoServicoOrderByIdDesc(entrevista.getId(), tipoServico)
                    .orElse(null);
        }

        return acolhimentoServicoRepository.findFirstByIdEntrevistaOrderByIdDesc(entrevista.getId())
                .orElse(null);
    }

    private OrientacaoServicoResponse mapearServico(AcolhimentoServico servico) {
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
}
