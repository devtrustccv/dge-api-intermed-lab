package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaFormularioResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagasResponse;
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
