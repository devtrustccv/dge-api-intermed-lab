package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CertificadoEstagioEmissaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CertificadoEstagioResponse;

public interface PerfilCandidatoCertificadoService {

    CertificadoEstagioResponse consultar(Integer colocacaoId, Long pessoaId);

    CertificadoEstagioResponse emitir(
            Integer colocacaoId,
            Long pessoaId,
            CertificadoEstagioEmissaoRequest request
    );

    CertificadoEstagioResponse validar(String codigoContraprova);
}

