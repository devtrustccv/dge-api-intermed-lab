package cv.dge.dge_api_intermed_lab.application.acolhimento.service;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEntidadeResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoPessoaResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.PacCandidaturaResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.UtenteResponse;
import java.util.List;

public interface AcolhimentoConsultaService {

    List<UtenteResponse> listarUtentes();

    List<UtenteResponse> listarUtentes(Integer cefpId, String denominacao);

    List<PacCandidaturaResponse> listarCandidaturas();

    List<PacCandidaturaResponse> listarCandidaturas(Integer cefpId, String denominacao);

    AcolhimentoPessoaResponse buscarPorIdPessoa(Integer idPessoa);

    AcolhimentoEntidadeResponse buscarPorIdEntidade(Integer globalIdEntidade);
}
