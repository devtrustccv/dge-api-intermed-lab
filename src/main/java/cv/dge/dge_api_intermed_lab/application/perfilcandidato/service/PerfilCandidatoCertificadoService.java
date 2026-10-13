package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.certificado.CertificadoEstagioEmissaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.certificado.CertificadoEstagioResponse;

public interface PerfilCandidatoCertificadoService {

    CertificadoEstagioResponse consultar(Integer colocacaoId);

    CertificadoEstagioResponse emitir(
            Integer colocacaoId,
            Long pessoaId,
            CertificadoEstagioEmissaoRequest request
    );

    CertificadoEstagioResponse validar(String codigoContraprova);
}

