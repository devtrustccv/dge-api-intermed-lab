package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CandidaturaVagaResponse(
        Integer id,
        Long pessoaId,
        String nomeCandidato,
        Integer ofertaId,
        String codigoReferencia,
        String tituloOferta,
        String tipoOferta,
        Integer entidadeId,
        String denominacaoEntidade,
        String canal,
        String statusCandidatura,
        String habilitacaoAcademica,
        String areaFormacao,
        CandidaturaDocumentoResponse curriculumVitae,
        List<CandidaturaDocumentoResponse> outrosDocumentos,
        LocalDateTime dataCandidatura
) {
}
