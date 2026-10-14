package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.candidatura.CandidaturaVagaFormularioResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.candidatura.CandidaturaVagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.candidatura.CandidaturaVagaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.vaga.ConsultaVagaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.vaga.ConsultaVagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.vaga.ConsultaVagaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.vaga.ConsultaVagasResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ConsultaVagaService {

    ConsultaVagasResponse listar(ConsultaVagaFiltro filtro);

    ConsultaVagaDetalheResponse buscarPorId(Integer ofertaId, Long pessoaId);

    ConsultaVagaOpcoesResponse listarOpcoes(Long ilhaId);

    CandidaturaVagaFormularioResponse buscarFormularioCandidatura(Integer ofertaId, Long pessoaId);

    CandidaturaVagaResponse candidatar(
            Integer ofertaId,
            Long pessoaId,
            CandidaturaVagaRequest request,
            MultipartFile curriculo,
            List<MultipartFile> outrosDocumentos
    );
}
